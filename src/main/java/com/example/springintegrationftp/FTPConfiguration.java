package com.example.springintegrationftp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.gateway.AbstractRemoteFileOutboundGateway;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.gateway.FtpOutboundGateway;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.util.List;

//https://medium.com/nerd-for-tech/retrieving-files-from-ftp-server-using-spring-integration-5ccc4a972eaf
@Configuration
public class FTPConfiguration {
    @Bean
    public DefaultFtpSessionFactory sf() {
        DefaultFtpSessionFactory sf = new DefaultFtpSessionFactory();
        sf.setHost("192.168.35.210");
        sf.setPort(21);
        sf.setUsername("ckbs");
        sf.setPassword("1111");
        return sf;
    }

    @ServiceActivator(inputChannel = "ftpMGET")
    @Bean
    public FtpOutboundGateway getFiles() {
        FtpOutboundGateway gateway = new FtpOutboundGateway(sf(), "mget", "payload");
        gateway.setAutoCreateDirectory(true);
        gateway.setLocalDirectory(new File("./download"));
        gateway.setFileExistsMode(FileExistsMode.REPLACE_IF_MODIFIED);
        gateway.setFilter(new AcceptOnceFileListFilter<>());
        gateway.setOutputChannelName("fileResults");
        gateway.setOption(AbstractRemoteFileOutboundGateway.Option.RECURSIVE);
        return gateway;
    }

    @Bean
    public MessageChannel fileResults() {
        DirectChannel channel = new DirectChannel();
        channel.addInterceptor(tap());
        return channel;
    }

    @Bean
    public WireTap tap() {
        return new WireTap("logging");
    }

    @ServiceActivator(inputChannel = "logging")
    @Bean
    public LoggingHandler logger() {
        LoggingHandler logger = new LoggingHandler(LoggingHandler.Level.INFO);
        logger.setLogExpressionString("'Files:' + payload");
        return logger;
    }

    @MessagingGateway(defaultRequestChannel = "ftpMGET", defaultReplyChannel = "fileResults")
    public interface GateFile {
        List<File> mget(String directory);
    }
}
