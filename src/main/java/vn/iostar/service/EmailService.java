package vn.iostar.service;

public interface EmailService {

	void sendOtp(String email, String otp, String subject);
}