package cn.wildfirechat.app.jpa;

import lombok.Data;

import javax.persistence.*;


@Data
@Entity
@Table(name = "text")
public class Announcement {
	/**|
	 *
	 */
	@Id
	@Column(length = 128)
	private String groupId;

	/**
	 *
	 */
	private String author;

	/**
	 *
	 */
	private String announcement;

	/**
	 *
	 */
	private long timestamp;

}
