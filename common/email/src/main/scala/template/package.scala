import config.domain.EmailConfiguration

package object template {
  def confirmUniversityHTML(
      username: String,
      password: String,
      sender: String
  ) = s"""<!DOCTYPE html>
                                |<html>
                                |  <head>
                                |    <meta charset="utf-8" />
                                |    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                                |    <title>Welcome to the Platform</title>
                                |    <style>
                                |      body {
                                |        font-family: Arial, sans-serif;
                                |        font-size: 16px;
                                |        line-height: 1.6;
                                |        margin: 0;
                                |        padding: 0;
                                |        background-color: white;
                                |      }
                                |      h1 {
                                |        color: #2c3e50;
                                |        font-size: 32px;
                                |        margin: 20px 0;
                                |      }
                                |      h2 {
                                |        color: #e74c3c;
                                |        font-size: 24px;
                                |        margin: 20px 0;
                                |      }
                                |      p {
                                |        margin: 10px 0;
                                |      }
                                |      a {
                                |        color: #2980b9;
                                |        text-decoration: none;
                                |      }
                                |      .container {
                                |        max-width: 600px;
                                |        margin: 0 auto;
                                |        padding: 20px;
                                |      }
                                |      .button {
                                |        display: inline-block;
                                |        background-color: #e74c3c;
                                |        color: white;
                                |        padding: 10px 20px;
                                |        border-radius: 5px;
                                |        text-decoration: none;
                                |      }
                                |    </style>
                                |  </head>
                                |  <body>
                                |    <div class="container">
                                |      <h1>Welcome to the Platform</h1>
                                |      <p>Dear User,</p>
                                |      <p>
                                |        Your account has been created successfully. Please use the following
                                |        login details to access the platform:
                                |      </p>
                                |      <p>
                                |        Username: <strong>$username</strong><br />
                                |        Password: <strong>$password</strong>
                                |      </p>
                                |      <p>
                                |        To log in, please visit <a href="#">https://campuscompass.com/login</a> and
                                |        enter your username and password.
                                |      </p>
                                |      <p>
                                |        If you have any questions or concerns, please contact our support team
                                |        at <a href="mailto:${sender}">support@platform.com</a>.
                                |      </p>
                                |      <p>Best regards,</p>
                                |      <p>The Platform Team</p>
                                |    </div>
                                |  </body>
                                |</html>
                                |""".stripMargin

  def applicationRegisteredTemplate(
      name: String,
      username: String,
      password: String,
      sender: String
  ) = s"""
          <!DOCTYPE html>
                                |<html>
                                |  <head>
                                |    <title>University Application Confirmation</title>
                                |  </head>
                                |  <body style="font-family: Arial, sans-serif;">
                                |    <h1>University Application Confirmation</h1>
                                |    <p>Dear $name,</p>
                                |    <p>Thank you for applying to our university. Your application has been successfully registered and is currently under review.</p>
                                |    <p>To continue with the application process, you will need to access our online platform. Please find your platform credentials below:</p>
                                |    <ul>
                                |      <li><strong>Username:</strong> $username</li>
                                |      <li><strong>Password:</strong> $password</li>
                                |    </ul>
                                |    <p>Please keep these credentials safe and secure, and do not share them with anyone.</p>
                                |        If you have any questions or concerns, please contact our support team
                                |        at <a href="mailto:${sender}">support@platform.com</a>.
                                |      </p>
                                |      <p>Best regards,</p>
                                |      <p>The Platform Team</p>
                                |  </body>
                                |</html>""".stripMargin
}
