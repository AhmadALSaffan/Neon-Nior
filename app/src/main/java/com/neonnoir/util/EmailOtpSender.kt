package com.neonnoir.util

import com.neonnoir.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailOtpSender {

    private val senderEmail    = BuildConfig.OTP_EMAIL
    private val senderPassword = BuildConfig.OTP_EMAIL_PASSWORD

    // Sends a styled OTP email to the given address on the IO thread
    suspend fun sendOtp(toEmail: String, otpCode: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val session = buildSmtpSession()
                val message = buildMessage(session, toEmail, otpCode)
                Transport.send(message)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // Configures Gmail SMTP session with TLS on port 587
    private fun buildSmtpSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth",            "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host",            "smtp.gmail.com")
            put("mail.smtp.port",            "587")
            put("mail.smtp.ssl.trust",       "smtp.gmail.com")
        }
        return Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(senderEmail, senderPassword)
        })
    }

    // Builds the MIME email with a styled HTML body
    private fun buildMessage(session: Session, toEmail: String, otpCode: String): MimeMessage {
        return MimeMessage(session).apply {
            setFrom(InternetAddress(senderEmail, "Neon Noir"))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
            subject = "Your Neon Noir Premiere Code"
            setContent(buildHtmlBody(otpCode), "text/html; charset=utf-8")
        }
    }

    // Returns a dark-themed HTML email body with the OTP code
    private fun buildHtmlBody(otp: String): String = """
        <!DOCTYPE html>
        <html>
        <body style="margin:0;padding:0;background-color:#0D0D0F;font-family:'Helvetica Neue',Arial,sans-serif;">
          <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
              <td align="center" style="padding:40px 16px;">
                <table width="480" cellpadding="0" cellspacing="0"
                       style="background:#1A1A1F;border-radius:16px;overflow:hidden;">

                  <!-- Header gradient bar -->
                  <tr>
                    <td height="4"
                        style="background:linear-gradient(90deg,#F06292,#CE93D8);"></td>
                  </tr>

                  <!-- Logo -->
                  <tr>
                    <td align="center" style="padding:32px 32px 8px;">
                      <p style="margin:0;font-size:26px;font-style:italic;font-weight:bold;
                                 color:#F06292;letter-spacing:2px;">NEON NOIR</p>
                      <p style="margin:4px 0 0;font-size:11px;letter-spacing:3px;
                                 color:#9E9E9E;text-transform:uppercase;">
                        The Digital Premiere Awaits
                      </p>
                    </td>
                  </tr>

                  <!-- Headline -->
                  <tr>
                    <td align="center" style="padding:24px 32px 8px;">
                      <p style="margin:0;font-size:22px;font-weight:bold;color:#FFFFFF;">
                        Verify Your Access
                      </p>
                      <p style="margin:12px 0 0;font-size:14px;color:#9E9E9E;line-height:1.6;">
                        Use the code below to complete your sign-up.<br/>
                        This code expires in <strong style="color:#F06292;">10 minutes</strong>.
                      </p>
                    </td>
                  </tr>

                  <!-- OTP Code -->
                  <tr>
                    <td align="center" style="padding:28px 32px;">
                      <table cellpadding="0" cellspacing="0">
                        <tr>
                          ${otp.map { digit ->
        """<td style="width:48px;height:60px;
                                           background:#25252B;
                                           border-radius:10px;
                                           text-align:center;
                                           vertical-align:middle;
                                           font-size:28px;
                                           font-weight:bold;
                                           color:#F06292;
                                           margin:0 4px;">
                                   $digit
                                 </td>
                                 <td width="6"></td>"""
    }.joinToString("")}
                        </tr>
                      </table>
                    </td>
                  </tr>

                  <!-- Warning -->
                  <tr>
                    <td align="center" style="padding:0 32px 32px;">
                      <p style="margin:0;font-size:12px;color:#616161;line-height:1.6;">
                        If you didn't request this code, you can safely ignore this email.<br/>
                        Never share this code with anyone.
                      </p>
                    </td>
                  </tr>

                  <!-- Footer -->
                  <tr>
                    <td style="background:#141418;padding:16px 32px;border-radius:0 0 16px 16px;">
                      <p style="margin:0;font-size:11px;color:#424242;text-align:center;
                                 letter-spacing:1px;text-transform:uppercase;">
                        © 2026 Neon Noir · All rights reserved
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>
        </body>
        </html>
    """.trimIndent()
}