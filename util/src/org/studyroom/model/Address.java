package org.studyroom.model;

/**migliorabile*/
public class Address {
	private String fullAddress,postalCode,city;
	public Address(String fullAddress, String postalCode, String city){
		this.fullAddress=fullAddress;
		this.postalCode=postalCode;
		this.city=city;
	}
	public String getFullAddress(){
		return fullAddress;
	}
	public String getPostalCode(){
		return postalCode;
	}
	public String getCity(){
		return city;
	}
	@Override
	public String toString(){
		return fullAddress;
	}
}
