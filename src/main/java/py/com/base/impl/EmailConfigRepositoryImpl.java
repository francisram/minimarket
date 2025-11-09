package py.com.base.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import py.com.base.clases.EmailConfig;
import py.com.base.interfaces.EmailConfigRepository;
import py.com.base.utils.AppConfig;

public class EmailConfigRepositoryImpl implements EmailConfigRepository {

    private DataSource dataSource;


    public EmailConfigRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    public EmailConfigRepositoryImpl() {
    }

    @Override
    public EmailConfig findByConnectionId(String connectionId) {
        String sql = "SELECT CNEUCNX, CNEDOMIN, CNEPASS, CNEPUERT FROM GXFINDTA.TCLCNE WHERE CNEIDCON = ?";
        EmailConfig emailConfig = null;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, connectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    emailConfig = new EmailConfig();
                    emailConfig.setUsername(rs.getString("CNEUCNX"));
                    emailConfig.setHost(rs.getString("CNEDOMIN"));
                    emailConfig.setPassword(rs.getString("CNEPASS"));
                    emailConfig.setPort(rs.getString("CNEPUERT"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emailConfig;
    }

	@Override
	public EmailConfig findFromProperties() {
	        EmailConfig emailConfig = null;
	            try  {
	                    emailConfig = new EmailConfig();
	                    emailConfig.setUsername(AppConfig.EMAILUSERNAME);
	                    emailConfig.setPassword(AppConfig.EMAILUSERPASS);
	                    emailConfig.setHost(AppConfig.EMAILDOMAIN);
	                    emailConfig.setPort(AppConfig.EMAILPORT);
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return emailConfig;
	}
}
