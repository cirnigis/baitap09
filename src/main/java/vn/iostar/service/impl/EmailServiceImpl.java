package vn.iostar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import vn.iostar.service.EmailService;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;

	@Override
	public void sendOtp(String email, String otp, String subject) {
		SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(email);
		message.setSubject(subject);

		message.setText("""
				Hello,

				Your OTP is: %s

				The OTP is valid for 5 minutes and can only be used once.
				Do not share this code with anyone.
				""".formatted(otp));

		mailSender.send(message);
	}
}