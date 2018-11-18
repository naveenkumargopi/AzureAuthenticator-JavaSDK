package com.thumbsignin.domain;

import java.security.acl.Group;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement
@Getter
@Setter
public class User extends DirectoryObject{
	
	// The following are the individual private members of a User object that holds
	// a particular simple attribute of an User object.
	protected String objectId;
	protected String objectType;
	protected String accountEnabled;
	protected String city;
	protected String country;
	protected String department;
	protected String dirSyncEnabled;
	protected String displayName;
	protected String facsimileTelephoneNumber;
	protected String givenName;
	protected String jobTitle;
	protected String lastDirSyncTime;
	protected String mail;
	protected String mailNickname;
	protected String mobile;
	protected String password;
	protected String passwordPolicies;
	protected String physicalDeliveryOfficeName;
	protected String postalCode;
	protected String preferredLanguage;
	protected String state;
	protected String streetAddress;
	protected String surname;
	protected String telephoneNumber;
	protected String usageLocation;
	protected String userPrincipalName;
	protected boolean isDeleted;  // this will move to dto

	/**
	 * below 4 properties are for future use
	 */
	// managerDisplayname of this user
	protected String managerDisplayname;
	
	// The directReports holds a list of directReports
	private ArrayList<User> directReports;
	
	// The groups holds a list of group entity this user belongs to. 
	private ArrayList<Group> groups;
	
	// The roles holds a list of role entity this user belongs to. 
	private ArrayList<Group> roles;
	
	
	/**
	 * The constructor for the User class. Initializes the dynamic lists and managerDisplayname variables.
	 */
	public User(){
		setDirectReports(null);
		setGroups(new ArrayList<Group>());
		setRoles(new ArrayList<Group>());
		managerDisplayname = null;
	}

}



