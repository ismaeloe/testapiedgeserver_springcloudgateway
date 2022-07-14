package mx.com.ismaeloe.apiedge_springcloudgateway.dto;

public class UserDto {

	private String user;
	private String name;
	
	public UserDto( ) { }
	public UserDto(String user, String name) {
		super();
		this.user = user;
		this.name = name;
	}

	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
	
}
