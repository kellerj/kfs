/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on Mar 10, 2005
 *
 */
package org.kuali.kfs.module.purap.util.cxml;

import org.apache.commons.lang.builder.ToStringBuilder;

public class CxmlHeader {
  private String fromIdentity;
  private String fromDomain;
  private String fromType;
  private String toIdentity;
  private String toDomain;
  private String toType;
  private String senderIdentity;
  private String senderDomain;
  private String senderType;
  private String senderUserAgent;
  
  /**
   * Newly Added
   */
  private String fromSharedSecret;
  private String toSharedSecret;
  private String senderSharedSecret;

  public CxmlHeader() {
    super();
  }

  public void setFrom(String domain,String identity) {
    this.setFrom(domain,identity,null);
  }

  public void setFrom(String domain,String identity,String type) {
    this.fromDomain = domain;
    this.fromIdentity = identity;
    this.fromType = type;
  }

  public void setTo(String domain,String identity) {
    this.setTo(domain,identity,null);
  }

  public void setTo(String domain,String identity,String type) {
    this.toDomain = domain;
    this.toIdentity = identity;
    this.toType = type;
  }

  public void setSender(String domain,String identity) {
    this.setSender(domain,identity,null);
  }

  public void setSender(String domain,String identity,String type) {
    this.senderDomain = domain;
    this.senderIdentity = identity;
    this.senderType = type;
  }

  /**
   * @return Returns the fromDomain.
   */
  public String getFromDomain() {
    return fromDomain;
  }
  /**
   * @param fromDomain The fromDomain to set.
   */
  public void setFromDomain(String fromDomain) {
    this.fromDomain = fromDomain;
  }
  /**
   * @return Returns the fromIdentity.
   */
  public String getFromIdentity() {
    return fromIdentity;
  }
  /**
   * @param fromIdentity The fromIdentity to set.
   */
  public void setFromIdentity(String fromIdentity) {
    this.fromIdentity = fromIdentity;
  }
  /**
   * @return Returns the fromType.
   */
  public String getFromType() {
    return fromType;
  }
  /**
   * @param fromType The fromType to set.
   */
  public void setFromType(String fromType) {
    this.fromType = fromType;
  }
  /**
   * @return Returns the senderDomain.
   */
  public String getSenderDomain() {
    return senderDomain;
  }
  /**
   * @param senderDomain The senderDomain to set.
   */
  public void setSenderDomain(String senderDomain) {
    this.senderDomain = senderDomain;
  }
  /**
   * @return Returns the senderIdentity.
   */
  public String getSenderIdentity() {
    return senderIdentity;
  }
  /**
   * @param senderIdentity The senderIdentity to set.
   */
  public void setSenderIdentity(String senderIdentity) {
    this.senderIdentity = senderIdentity;
  }
  /**
   * @return Returns the senderType.
   */
  public String getSenderType() {
    return senderType;
  }
  /**
   * @param senderType The senderType to set.
   */
  public void setSenderType(String senderType) {
    this.senderType = senderType;
  }
  /**
   * @return Returns the senderUserAgent.
   */
  public String getSenderUserAgent() {
    return senderUserAgent;
  }
  /**
   * @param senderUserAgent The senderUserAgent to set.
   */
  public void setSenderUserAgent(String senderUserAgent) {
    this.senderUserAgent = senderUserAgent;
  }
  /**
   * @return Returns the toDomain.
   */
  public String getToDomain() {
    return toDomain;
  }
  /**
   * @param toDomain The toDomain to set.
   */
  public void setToDomain(String toDomain) {
    this.toDomain = toDomain;
  }
  /**
   * @return Returns the toIdentity.
   */
  public String getToIdentity() {
    return toIdentity;
  }
  /**
   * @param toIdentity The toIdentity to set.
   */
  public void setToIdentity(String toIdentity) {
    this.toIdentity = toIdentity;
  }
  /**
   * @return Returns the toType.
   */
  public String getToType() {
    return toType;
  }
  /**
   * @param toType The toType to set.
   */
  public void setToType(String toType) {
    this.toType = toType;
  }
  
  public String getSenderSharedSecret() {
      return senderSharedSecret;
  }

  public void setSenderSharedSecret(String senderSharedSecret) {
      this.senderSharedSecret = senderSharedSecret;
  }
  
  public String getFromSharedSecret() {
      return fromSharedSecret;
  }

  public void setFromSharedSecret(String fromSharedSecret) {
      this.fromSharedSecret = fromSharedSecret;
  }

  public String getToSharedSecret() {
      return toSharedSecret;
  }

  public void setToSharedSecret(String toSharedSecret) {
      this.toSharedSecret = toSharedSecret;
  }
  
  public String toString(){
      
      ToStringBuilder toString = new ToStringBuilder(this);
      
      toString.append("FromDomain",getFromDomain());
      toString.append("FromIdentity",getFromIdentity());
      toString.append("FromSharedSecret",getFromSharedSecret());
      toString.append("FromType",getFromType());
      
      toString.append("ToDomain",getToDomain());
      toString.append("ToIdentity",getToIdentity());
      toString.append("ToSharedSecret",getToSharedSecret());
      toString.append("ToType",getToType());
      
      toString.append("SenderDomain",getSenderDomain());
      toString.append("SenderIdentity",getSenderIdentity());
      toString.append("SenderType",getSenderType());
      toString.append("SenderSharedSecret",getSenderSharedSecret());
      toString.append("SenderUserAgent",getSenderUserAgent());
      
      return toString.toString();
  }



}
