package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.TourRequestDTO;
import com.example.RoomBooking.models.TourRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendConfirmationEmail(TourRequestDTO tourRequest) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(tourRequest.getEmail());
            helper.setSubject("🏠 Xác nhận yêu cầu tham quan bất động sản");
            helper.setText(createEmailContent(tourRequest), true);
            helper.setFrom(fromEmail);

            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String createEmailContent(TourRequestDTO tourRequest) {
        return String.format(
                "<html><body style='font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px;'>" +
                        "<div style='background-color: #ffffff; border-radius: 5px; padding: 20px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                        "<h2 style='color: #2c3e50; text-align: center;'>🏠 Xác nhận yêu cầu tham quan bất động sản</h2>" +
                        "<p>Kính gửi Quý khách,</p>" +
                        "<p>Chúng tôi xin xác nhận đã nhận được yêu cầu tham quan bất động sản của Quý khách. Dưới đây là thông tin chi tiết:</p>" +
                        "<ul style='list-style-type: none; padding: 0;'>" +
                        "<li>🔑 Mã yêu cầu: <strong>%d</strong></li>" +
                        "<li>📅 Ngày tham quan: <strong>%s</strong></li>" +
                        "<li>🕒 Thời gian: <strong>%s</strong></li>" +
                        "<li>🏡 Mã bất động sản: <strong>%d</strong></li>" +
                        "</ul>" +
                        "<h3 style='color: #2c3e50;'>Các bước tiếp theo:</h3>" +
                        "<ol>" +
                        "<li>Chúng tôi sẽ xem xét yêu cầu của Quý khách và liên hệ để xác nhận chi tiết.</li>" +
                        "<li>Vui lòng chuẩn bị giấy tờ tùy thân khi đến tham quan.</li>" +
                        "<li>Nếu có bất kỳ thay đổi nào, xin vui lòng thông báo cho chúng tôi sớm nhất có thể.</li>" +
                        "</ol>" +
                        "<p><a href='http://localhost:3000/client-requests?email=%s' style='color: #3498db; text-decoration: none;'>Xem lại toàn bộ yêu cầu hoặc thay đổi lịch trình tại đây</a></p>" +
                        "<p>Nếu Quý khách có bất kỳ câu hỏi nào, xin đừng ngần ngại liên hệ với chúng tôi.</p>" +
                        "<p>Trân trọng,<br>Đội ngũ Chăm sóc Khách hàng 👋</p>" +
                        "</div>" +
                        "</body></html>",
                tourRequest.getId(),
                tourRequest.getAppointmentDate(),
                tourRequest.getAppointmentTime(),
                tourRequest.getPropertyId(),
                tourRequest.getEmail()
        );
    }

    @Async
    public void sendStatusUpdateEmail(TourRequestDTO tourRequest, TourRequest.TourStatus oldStatus, TourRequest.TourStatus newStatus) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(tourRequest.getEmail());
            helper.setSubject("🔄 Cập nhật trạng thái yêu cầu tham quan");
            helper.setText(createStatusUpdateEmailContent(tourRequest, oldStatus, newStatus), true);
            helper.setFrom(fromEmail);

            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String createStatusUpdateEmailContent(TourRequestDTO tourRequest, TourRequest.TourStatus oldStatus, TourRequest.TourStatus newStatus) {
        return String.format(
                "<html><body style='font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px;'>" +
                        "<div style='background-color: #ffffff; border-radius: 5px; padding: 20px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                        "<h2 style='color: #2c3e50; text-align: center;'>🔄 Cập nhật trạng thái yêu cầu tham quan</h2>" +
                        "<p>Kính gửi Quý khách,</p>" +
                        "<p>Chúng tôi xin thông báo về sự thay đổi trong trạng thái yêu cầu tham quan của Quý khách:</p>" +
                        "<ul style='list-style-type: none; padding: 0;'>" +
                        "<li>🔑 Mã yêu cầu: <strong>%d</strong></li>" +
                        "<li>📅 Ngày tham quan: <strong>%s</strong></li>" +
                        "<li>🕒 Thời gian: <strong>%s</strong></li>" +
                        "<li>🏡 Mã bất động sản: <strong>%d</strong></li>" +
                        "<li>🔙 Trạng thái cũ: <strong>%s</strong></li>" +
                        "<li>🆕 Trạng thái mới: <strong>%s</strong></li>" +
                        "</ul>" +
                        "<p>Nếu Quý khách có bất kỳ thắc mắc nào về sự thay đổi này, xin vui lòng liên hệ với chúng tôi để được giải đáp.</p>" +
                        "<p>Trân trọng,<br>Đội ngũ Chăm sóc Khách hàng 👋</p>" +
                        "</div>" +
                        "</body></html>",
                tourRequest.getId(),
                tourRequest.getAppointmentDate(),
                tourRequest.getAppointmentTime(),
                tourRequest.getPropertyId(),
                oldStatus,
                newStatus
        );
    }

    @Async
    public void sendReminderEmail(TourRequestDTO tourRequest, String toEmail) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("⏰ Nhắc nhở: Lịch tham quan bất động sản ngày mai");
            helper.setText(createReminderEmailContent(tourRequest), true);
            helper.setFrom(fromEmail);

            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String createReminderEmailContent(TourRequestDTO tourRequest) {
        return String.format(
                "<html><body style='font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px;'>" +
                        "<div style='background-color: #ffffff; border-radius: 5px; padding: 20px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
                        "<h2 style='color: #2c3e50; text-align: center;'>⏰ Nhắc nhở: Lịch tham quan bất động sản ngày mai</h2>" +
                        "<p>Kính gửi Quý khách,</p>" +
                        "<p>Chúng tôi xin gửi lời nhắc nhở về lịch tham quan bất động sản của Quý khách vào ngày mai:</p>" +
                        "<ul style='list-style-type: none; padding: 0;'>" +
                        "<li>🔑 Mã yêu cầu: <strong>%d</strong></li>" +
                        "<li>📅 Ngày tham quan: <strong>%s</strong> (Ngày mai)</li>" +
                        "<li>🕒 Thời gian: <strong>%s</strong></li>" +
                        "<li>🏡 Mã bất động sản: <strong>%d</strong></li>" +
                        "</ul>" +
                        "<h3 style='color: #2c3e50;'>Lưu ý:</h3>" +
                        "<ul>" +
                        "<li>✅ Vui lòng đảm bảo Quý khách đã sẵn sàng cho cuộc hẹn này.</li>" +
                        "<li>📄 Mang theo giấy tờ tùy thân cần thiết.</li>" +
                        "<li>📞 Nếu có bất kỳ thay đổi nào, xin vui lòng thông báo cho chúng tôi sớm nhất có thể.</li>" +
                        "</ul>" +
                        "<p>Nếu Quý khách có bất kỳ câu hỏi nào, xin đừng ngần ngại liên hệ với chúng tôi.</p>" +
                        "<p>Trân trọng,<br>Đội ngũ Chăm sóc Khách hàng 👋</p>" +
                        "</div>" +
                        "</body></html>",
                tourRequest.getId(),
                tourRequest.getAppointmentDate(),
                tourRequest.getAppointmentTime(),
                tourRequest.getPropertyId()
        );
    }

    @Async
    public void sendResetPasswordEmail(String email, String fullName, String token) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String htmlContent = createResetPasswordEmailContent(fullName, token);

        helper.setTo(email);
        helper.setSubject("🔐 Yêu cầu đặt lại mật khẩu");
        helper.setText(htmlContent, true);
        helper.setFrom(fromEmail);

        emailSender.send(mimeMessage);
    }

    private String createResetPasswordEmailContent(String fullName, String token) {
        return "<html><body style='font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px;'>"
                + "<div style='background-color: #ffffff; border-radius: 5px; padding: 20px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #2c3e50; text-align: center;'>🔐 Yêu cầu đặt lại mật khẩu</h2>"
                + "<p>Kính gửi " + fullName + ",</p>"
                + "<p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của Quý khách. Để tiếp tục quá trình này, vui lòng nhấp vào nút bên dưới:</p>"
                + "<div style='text-align: center;'>"
                + "<a href='http://localhost:3000/reset-password?token=" + token + "' "
                + "style='background-color: #3498db; border: none; color: white; padding: 15px 32px; "
                + "text-align: center; text-decoration: none; display: inline-block; font-size: 16px; "
                + "margin: 4px 2px; cursor: pointer; border-radius: 5px;'>"
                + "🔑 Đặt lại mật khẩu</a>"
                + "</div>"
                + "<p>⚠️ Lưu ý: Liên kết này sẽ hết hạn sau 24 giờ vì lý do bảo mật.</p>"
                + "<p>Nếu Quý khách không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này hoặc liên hệ với bộ phận hỗ trợ của chúng tôi nếu có bất kỳ thắc mắc nào.</p>"
                + "<p>Trân trọng,<br>Đội ngũ Bảo mật 🛡️</p>"
                + "</div>"
                + "</body></html>";
    }

    @Async
    public void sendContactFormEmail(String name, String email, String message) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String htmlContent = createContactFormEmailContent(name, email, message);

        helper.setTo(fromEmail); // Gửi đến địa chỉ email của công ty
        helper.setSubject("📬 Tin nhắn mới từ biểu mẫu liên hệ");
        helper.setText(htmlContent, true);
        helper.setFrom(fromEmail);

        emailSender.send(mimeMessage);
    }

    private String createContactFormEmailContent(String name, String email, String message) {
        return "<html><body style='font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px;'>"
                + "<div style='background-color: #ffffff; border-radius: 5px; padding: 20px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #2c3e50; text-align: center;'>📬 Tin nhắn mới từ biểu mẫu liên hệ</h2>"
                + "<p>Kính gửi đội ngũ quản lý,</p>"
                + "<p>Chúng ta vừa nhận được một tin nhắn mới từ biểu mẫu liên hệ trên website. Dưới đây là chi tiết:</p>"
                + "<ul style='list-style-type: none; padding: 0;'>"
                + "<li>👤 <strong>Tên:</strong> " + name + "</li>"
                + "<li>📧 <strong>Email:</strong> " + email + "</li>"
                + "<li>💬 <strong>Nội dung tin nhắn:</strong></li>"
                + "</ul>"
                + "<div style='background-color: #f2f2f2; border-left: 4px solid #3498db; padding: 10px; margin: 10px 0;'>"
                + "<p style='font-style: italic;'>" + message + "</p>"
                + "</div>"
                + "<p>Vui lòng xem xét và phản hồi tin nhắn này trong thời gian sớm nhất.</p>"
                + "<p>Trân trọng,<br>Hệ thống Quản lý Liên hệ 🤖</p>"
                + "</div>"
                + "</body></html>";
    }

    @Async
    public void sendPropertyInquiryEmail(String name, String email, String phoneNumber, String propertyId, String message) throws MessagingException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String htmlContent = createPropertyInquiryEmailContent(name, email, phoneNumber, propertyId, message);

        helper.setTo(fromEmail); // Gửi đến địa chỉ email của công ty
        helper.setSubject("🏠 Yêu cầu thông tin bất động sản mới");
        helper.setText(htmlContent, true);
        helper.setFrom(fromEmail);

        emailSender.send(mimeMessage);
    }

    private String createPropertyInquiryEmailContent(String name, String email, String phoneNumber, String propertyId, String message) {
        return "<html><body style='font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px;'>"
                + "<div style='background-color: #ffffff; border-radius: 5px; padding: 20px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #2c3e50; text-align: center;'>🏠 Yêu cầu thông tin bất động sản mới</h2>"
                + "<p>Kính gửi đội ngũ kinh doanh,</p>"
                + "<p>Chúng ta vừa nhận được một yêu cầu thông tin về bất động sản. Dưới đây là chi tiết:</p>"
                + "<ul style='list-style-type: none; padding: 0;'>"
                + "<li>👤 <strong>Tên:</strong> " + name + "</li>"
                + "<li>📧 <strong>Email:</strong> " + email + "</li>"
                + "<li>📞 <strong>Số điện thoại:</strong> " + phoneNumber + "</li>"
                + "<li>🔑 <strong>Mã bất động sản:</strong> " + propertyId + "</li>"
                + "<li>💬 <strong>Nội dung yêu cầu:</strong></li>"
                + "</ul>"
                + "<div style='background-color: #f2f2f2; border-left: 4px solid #3498db; padding: 10px; margin: 10px 0;'>"
                + "<p style='font-style: italic;'>" + message + "</p>"
                + "</div>"
                + "<p>Vui lòng liên hệ với khách hàng này trong thời gian sớm nhất để cung cấp thông tin chi tiết về bất động sản.</p>"
                + "<p>Trân trọng,<br>Hệ thống Quản lý Yêu cầu 🤖</p>"
                + "</div>"
                + "</body></html>";
    }
}