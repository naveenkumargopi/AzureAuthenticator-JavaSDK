package com.thumbsignin.domain;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

/**
 *  The Membership Class holds together all the information about WAAD Memberships (Groups or Directory Roles)
 *  @author Naveen Kumar G
 */
@XmlRootElement
@Getter
@Setter
public class Membership extends DirectoryObject {

	protected String objectId;
	protected String objectType;
	protected String displayName;
	protected String description;
	
}
