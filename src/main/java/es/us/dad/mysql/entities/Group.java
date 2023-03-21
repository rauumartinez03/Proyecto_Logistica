package es.us.dad.mysql.entities;

/**
 * This entity represents a grouping of devices that share certain business
 * logic characteristics and whose sensors and actuators are connected in some
 * logical (not necessarily physical) way.
 * 
 * @author luismi
 *
 */
public class Group {

	/**
	 * Group identifier
	 */
	private Integer idGroup;

	/**
	 * MQTT channel that is shared by all devices in the group represented by this
	 * entity. All the messages that must be exchanged between the service and all
	 * the devices of a certain group will be done through this channel.
	 */
	private String mqttChannel;

	/**
	 * Group name
	 */
	private String name;

	/**
	 * Timestamp of the last message received through a device in this group.
	 */
	private Long lastMessageReceived;

	public Group() {
		super();
	}

	public Group(String mqttChannel, String name, Long lastMessageReceived) {
		super();
		this.mqttChannel = mqttChannel;
		this.name = name;
		this.lastMessageReceived = lastMessageReceived;
	}

	public Group(int idGroup, String mqttChannel, String name, Long lastMessageReceived) {
		super();
		this.idGroup = idGroup;
		this.mqttChannel = mqttChannel;
		this.name = name;
		this.lastMessageReceived = lastMessageReceived;
	}

	public Integer getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(Integer idGroup) {
		this.idGroup = idGroup;
	}

	public String getMqttChannel() {
		return mqttChannel;
	}

	public void setMqttChannel(String mqttChannel) {
		this.mqttChannel = mqttChannel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getLastMessageReceived() {
		return lastMessageReceived;
	}

	public void setLastMessageReceived(Long lastMessageReceived) {
		this.lastMessageReceived = lastMessageReceived;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idGroup == null) ? 0 : idGroup.hashCode());
		result = prime * result + ((lastMessageReceived == null) ? 0 : lastMessageReceived.hashCode());
		result = prime * result + ((mqttChannel == null) ? 0 : mqttChannel.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (idGroup == null) {
			if (other.idGroup != null)
				return false;
		} else if (!idGroup.equals(other.idGroup))
			return false;
		if (lastMessageReceived == null) {
			if (other.lastMessageReceived != null)
				return false;
		} else if (!lastMessageReceived.equals(other.lastMessageReceived))
			return false;
		if (mqttChannel == null) {
			if (other.mqttChannel != null)
				return false;
		} else if (!mqttChannel.equals(other.mqttChannel))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Group [idGroup=" + idGroup + ", mqttChannel=" + mqttChannel + ", name=" + name
				+ ", lastMessageReceived=" + lastMessageReceived + "]";
	}

}
