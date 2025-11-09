package py.com.base.clases;

public class EmailConfig {
    private String connectionId;
    private String host;
    private String port;
    private String username;
    private String password;


    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
