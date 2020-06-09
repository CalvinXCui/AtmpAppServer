package cn.wildfirechat.app.jpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@Table(name = "shiro_session")
public class ShiroSession {
	/**
	 *
	 */
	@Id
	@Column(length = 128)
	private String sessionId;

	/**
	 *
	 */
	@Lob
	@Column(name="session_data", length = 2048)
	@Type(type="org.hibernate.type.BinaryType")
	private byte[] sessionData;
}
