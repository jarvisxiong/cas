package com.inmobi.adserve.channels.util;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Created by ishanbhatnagar on 19/1/15.
 */
public class LoggerUtils {
    private static final Logger LOG = LoggerFactory.getLogger(LoggerUtils.class);

    public static void configureApplicationLoggers(Configuration loggerConfiguration) {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();

        try {
            configurator.doConfigure(loggerConfiguration.getString("loggerConf"));
        } catch (final JoranException e) {
            throw new RuntimeException(e);
        }
    }

    // Send eMail if channel server crashes
    @SuppressWarnings("unchecked")
    public static void sendMail(final String errorMessage, final String stackTrace,
                                 Configuration serverConfiguration) {
        final Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", serverConfiguration.getString("smtpServer"));
        final Session session = Session.getDefaultInstance(properties);
        try {
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(serverConfiguration.getString("sender")));
            final List<String> recipients = serverConfiguration.getList("recipients");
            final javax.mail.internet.InternetAddress[] addressTo =
                    new javax.mail.internet.InternetAddress[recipients.size()];

            for (int index = 0; index < recipients.size(); index++) {
                addressTo[index] = new javax.mail.internet.InternetAddress(recipients.get(index));
            }

            message.setRecipients(Message.RecipientType.TO, addressTo);
            final InetAddress addr = InetAddress.getLocalHost();
            message.setSubject("Channel Ad Server Crashed on Host " + addr.getHostName());
            message.setText(errorMessage + stackTrace);
            Transport.send(message);
        } catch (final MessagingException mex) {
            if (null != LOG) {
                LOG.error("MessagingException raised while sending mail " + mex);
            } else {
                System.out.println("MessagingException raised while sending mail " + mex);
            }
        } catch (final UnknownHostException ex) {
            if (null != LOG) {
                LOG.error("UnknownException raised while sending mail " + ex);
            } else {
                System.out.println("UnknownException raised while sending mail " + ex);
            }
        }
    }

    /**
     * Check if all log folders exist
     */
    public static boolean checkLogFolders(final Configuration config) {
        String debugLogFolder = config.getString("debug.File");
        String advertiserLogFolder = config.getString("advertiser.File");
        String sampledAdvertiserLogFolder = config.getString("sampledadvertiser.File");
        String repositoryLogFolder = config.getString("repository.File");
        String traceLogFolder = config.getString("trace.File");
        File debugFolder = null;
        File advertiserFolder = null;
        File sampledAdvertiserFolder = null;
        File repositoryFolder = null;
        File traceFolder = null;
        if (repositoryLogFolder != null) {
            repositoryLogFolder = repositoryLogFolder.substring(0, repositoryLogFolder.lastIndexOf('/') + 1);
            repositoryFolder = new File(repositoryLogFolder);
        }
        if (debugLogFolder != null) {
            debugLogFolder = debugLogFolder.substring(0, debugLogFolder.lastIndexOf('/') + 1);
            debugFolder = new File(debugLogFolder);
        }
        if (advertiserLogFolder != null) {
            advertiserLogFolder = advertiserLogFolder.substring(0, advertiserLogFolder.lastIndexOf('/') + 1);
            advertiserFolder = new File(advertiserLogFolder);
        }
        if (sampledAdvertiserLogFolder != null) {
            sampledAdvertiserLogFolder =
                    sampledAdvertiserLogFolder.substring(0, sampledAdvertiserLogFolder.lastIndexOf('/') + 1);
            sampledAdvertiserFolder = new File(sampledAdvertiserLogFolder);
        }
        if (traceLogFolder != null) {
            traceLogFolder = traceLogFolder.substring(0, traceLogFolder.lastIndexOf('/') + 1);
            traceFolder = new File(traceLogFolder);
        }
        if (debugFolder != null && debugFolder.exists() && advertiserFolder != null && advertiserFolder.exists() &&
                sampledAdvertiserFolder != null && sampledAdvertiserFolder.exists() && repositoryFolder != null &&
                repositoryFolder.exists() && traceFolder != null && traceFolder.exists()) {
            return true;
        }
        return false;
    }
}
