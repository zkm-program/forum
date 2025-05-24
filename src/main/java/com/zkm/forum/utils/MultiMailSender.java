package com.zkm.forum.utils;

import com.zkm.forum.common.ErrorCode;

import com.zkm.forum.exception.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
@Component
@Log4j2
public class MultiMailSender {
//    @Resource
//    private static MailConfig mailConfig;
    // 邮箱提供商配置
    private enum MailProvider {
        QQ("smtp.qq.com", "465"),
        NET163("smtp.163.com", "465"),
        CT189("smtp.189.cn", "465");

        final String host;
        final String port;

        MailProvider(String host, String port) {
            this.host = host;
            this.port = port;
        }
    }

    // 邮箱账户配置
    private static final List<MailAccount> MAIL_ACCOUNTS = Arrays.asList(
//            new MailAccount(MailProvider.QQ, mailConfig.getAcountQq(), mailConfig.getPasswordQq(), "卡塞尔学院诺玛"),
//            new MailAccount(MailProvider.NET163, mailConfig.getAcount163(), mailConfig.getPassword163(), "卡塞尔学院诺玛"),
//            new MailAccount(MailProvider.CT189, mailConfig.getAcount189(), mailConfig.getPassword189(), "卡塞尔学院诺玛")
            new MailAccount(MailProvider.QQ, "3149611191@qq.com", " ", "卡塞尔学院诺玛"),
            new MailAccount(MailProvider.NET163, " ", " ", "卡塞尔学院诺玛"),
            new MailAccount(MailProvider.CT189, " ", " ", "卡塞尔学院诺玛")
    );

    // 轮询计数器
    private static final AtomicInteger currentIndex = new AtomicInteger(0);

    // 邮箱状态记录（true表示可用）
    private static final Map<MailProvider, Boolean> providerStatus = new EnumMap<>(MailProvider.class);

    static {
        // 初始化所有邮箱状态为可用
        Arrays.stream(MailProvider.values()).forEach(provider -> providerStatus.put(provider, true));
        //给qq和网易邮箱设置成false
//        providerStatus.put(MailProvider.QQ, false);
////        providerStatus.put(MailProvider.NET163, false);
//        providerStatus.put(MailProvider.CT189, false);

    }

    private static class MailAccount {
        final MailProvider provider;
        final String username;
        final String password;
        final String fromName;

        MailAccount(MailProvider provider, String username, String password, String fromName) {
            this.provider = provider;
            this.username = username;
            this.password = password;
            this.fromName = fromName;
        }
    }

    /**
     * 发送邮件（支持多邮箱轮询）
     */
    public static boolean sendEmail(Set<String> emails, String title, String content) {
        if (emails == null || emails.isEmpty()) return false;

        int retryCount = 0;
        int maxRetry = MAIL_ACCOUNTS.size();

        while (retryCount < maxRetry) {
            MailAccount account = getNextAvailableAccount();

            try {
                boolean success = sendWithAccount(account, emails, title, content);
                if (success) {
                    providerStatus.put(account.provider, true);
                    return true;
                }
            } catch (Exception e) {
                log.error("邮箱 {} 发送失败: {}", account.username, e.getMessage());
                providerStatus.put(account.provider, false);
                handleSendFailure(account.provider, e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮箱发送失败,请稍后重试");
            }

            retryCount++;
        }

        log.error("所有邮箱尝试发送均失败");
        return false;
    }

    private static MailAccount getNextAvailableAccount() {
        int startIdx = currentIndex.getAndIncrement() % MAIL_ACCOUNTS.size();
        int idx = startIdx;

        do {
            MailAccount account = MAIL_ACCOUNTS.get(idx);
            if (providerStatus.get(account.provider)) {
                return account;
            }
            idx = (idx + 1) % MAIL_ACCOUNTS.size();
        } while (idx != startIdx);

        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "没有可用的邮件发送账户");
    }

    private static boolean sendWithAccount(MailAccount account, Set<String> emails,
                                           String title, String content) throws MessagingException, UnsupportedEncodingException {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", account.provider.host);
        props.setProperty("mail.smtp.port", account.provider.port);
        props.setProperty("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.connectiontimeout", "10000");
        props.setProperty("mail.smtp.timeout", "10000");
        props.setProperty("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(props);
        session.setDebug(false);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(account.username, account.fromName, "UTF-8"));

        if (emails.size() == 1) {
            String email = emails.iterator().next();
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email, email, "UTF-8"));
        } else {
            InternetAddress[] addresses = emails.stream()
                    .map(email -> {
                        try {
                            return new InternetAddress(email, email, "UTF-8");
                        } catch (Exception e) {
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误: " + email);
                        }
                    })
                    .toArray(InternetAddress[]::new);
            message.setRecipients(Message.RecipientType.TO, addresses);
        }

        message.setSubject(title, "UTF-8");
        message.setContent(content, "text/html;charset=UTF-8");
        message.setSentDate(new Date());
        message.saveChanges();

        Transport transport = session.getTransport();
        try {
            transport.connect(account.username, account.password);
            transport.sendMessage(message, message.getAllRecipients());
            log.info("使用 {} 邮箱成功发送邮件", account.provider);
            return true;
        } finally {
            transport.close();
        }
    }

    private static void handleSendFailure(MailProvider provider, Exception e) {
        if (e instanceof SendFailedException) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("exceed") || msg.contains("limit") || msg.contains("quota")) {
                log.warn("邮箱 {} 可能达到发送限制，暂时禁用", provider);
                providerStatus.put(provider, false);
            }
        }
    }

    // 测试方法
    public static void main(String[] args) {
        Set<String> recipients = new HashSet<>();
        recipients.add("3149611191@qq.com");

        boolean result = sendEmail(recipients, "测试邮件", "这是一封测试邮件");
        System.out.println("发送结果: " + (result ? "成功" : "失败"));
    }
}
