package com.pearnode.app.placero.area.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.pearnode.app.placero.tags.Tag;

/**
 * Created by USER on 11/27/2017.
 */
public class Address {

    private String premises = "";
    private String subThoroughFare = "";
    private String thoroughFare = "";
    private String locality = "";
    private String subLocality = "";
    private String adminArea = "";
    private String subAdminArea = "";
    private String featureName = "";
    private String postalCode = "";
    private String country = "";

    private String storable = "";
    private String displayable = "";
    private List<Tag> tags = new ArrayList<>();

    public Address(){
    }

    public String getStorableAddress() {
        StringBuffer buf = new StringBuffer();

        // Add featureName.
        if(featureName != null){
            buf.append(featureName);
        }
        buf.append("@$");

        // Add premises.
        if(premises != null){
            if(featureName != null){
                if(!featureName.equalsIgnoreCase(premises)){
                    buf.append(premises);
                }
            }
        }
        buf.append("@$");

        // Add thoroughFare.
        if(thoroughFare != null){
            if(premises != null){
                if(!premises.equalsIgnoreCase(thoroughFare)){
                    buf.append(thoroughFare);
                }
            }
        }
        buf.append("@$");

        // Add subThoroughFare.
        if(subThoroughFare != null){
            buf.append(subThoroughFare);
        }
        buf.append("@$");

        // Add subLocality.
        if(subLocality != null){
            buf.append(subLocality);
        }
        buf.append("@$");

        // Add locality.
        if(locality != null){
            buf.append(locality);
        }
        buf.append("@$");

        // Add subAdminArea.
        if(subAdminArea != null){
            buf.append(subAdminArea);
        }
        buf.append("@$");

        // Add adminArea.
        if(adminArea != null){
            buf.append(adminArea);
            buf.append(",");
        }
        buf.append("@$");

        // Add postalCode.
        if(postalCode != null){
            buf.append(postalCode);
        }
        buf.append("@$");

        // Add country.
        if(country != null){
            buf.append(country);
        }
        buf.append("@$");
        storable = buf.toString();
        return storable;
    }

    public String getDisplaybleAddress(){
        String storableAddress = getStorableAddress();
        storableAddress = storableAddress.replaceAll(Pattern.quote("@$"), ",");
        storableAddress = storableAddress.replaceAll(",{2,}", ",");
        storableAddress = storableAddress.trim();
        storableAddress = storableAddress.replaceAll(",$", "");
        storableAddress = storableAddress.replaceFirst("^,", "");
        displayable = storableAddress;
        return displayable;
    }

    public static final Address fromStoredAddress(String addressText){
        String[] splittedAddress = addressText.split(Pattern.quote("@$"));
        if(splittedAddress.length != 10){
            return null;
        }
        Address address = new Address();
        address.setPremises(splittedAddress[0]);
        address.setFeatureName(splittedAddress[1]);
        address.setSubThoroughFare(splittedAddress[2]);
        address.setThoroughFare(splittedAddress[3]);
        address.setSubLocality(splittedAddress[4]);
        address.setLocality(splittedAddress[5]);
        address.setSubAdminArea(splittedAddress[6]);
        address.setAdminArea(splittedAddress[7]);
        address.setPostalCode(splittedAddress[8]);
        address.setCountry(splittedAddress[9]);
        return address;
    }

    public List<Tag> getTags(){
        if(premises != null && !premises.trim().equalsIgnoreCase("")){
            premises = premises.replaceAll("[-+.^:,]","");
            if(!premises.matches("[0-9]+")){
                String[] split = premises.split(" ");
                if(split.length > 1){
                    for (int i = 0; i < split.length; i++) {
                        if(!split[i].matches("[0-9]+")){
                            tags.add(new Tag(split[i], "filterable", "address"));
                        }
                    }
                }
                tags.add(new Tag(premises, "filterable", "address"));
            }
        }
        if(subLocality != null && !subLocality.trim().equalsIgnoreCase("")){
            subLocality = subLocality.replaceAll("[-+.^:,]","");
            if(!subLocality.matches("[0-9]+")) {
                String[] split = subLocality.split(" ");
                if(split.length > 1){
                    for (int i = 0; i < split.length; i++) {
                        if(!split[i].matches("[0-9]+")){
                            tags.add(new Tag(split[i], "filterable", "address"));
                        }
                    }
                }
                tags.add(new Tag(subLocality, "filterable", "address"));
            }
        }
        if(locality != null && !locality.trim().equalsIgnoreCase("")){
            locality = locality.replaceAll("[-+.^:,]","");
            if(!locality.matches("[0-9]+")) {
                String[] split = locality.split(" ");
                if(split.length > 1){
                    for (int i = 0; i < split.length; i++) {
                        if(!split[i].matches("[0-9]+")){
                            tags.add(new Tag(split[i], "filterable", "address"));
                        }
                    }
                }
                tags.add(new Tag(locality, "filterable", "address"));
            }
        }
        if(subAdminArea != null && !subAdminArea.trim().equalsIgnoreCase("")){
            subAdminArea = subAdminArea.replaceAll("[-+.^:,]","");
            if(!subAdminArea.matches("[0-9]+")) {
                String[] split = subAdminArea.split(" ");
                if(split.length > 1){
                    for (int i = 0; i < split.length; i++) {
                        if(!split[i].matches("[0-9]+")){
                            tags.add(new Tag(split[i], "filterable", "address"));
                        }
                    }
                }
                tags.add(new Tag(subAdminArea, "filterable", "address"));
            }
        }
        if(adminArea != null && !adminArea.trim().equalsIgnoreCase("")){
            adminArea = adminArea.replaceAll("[-+.^:,]","");
            if(!adminArea.matches("[0-9]+")) {
                String[] split = adminArea.split(" ");
                if(split.length > 1){
                    for (int i = 0; i < split.length; i++) {
                        if(!split[i].matches("[0-9]+")){
                            tags.add(new Tag(split[i], "filterable", "address"));
                        }
                    }
                }
                tags.add(new Tag(adminArea, "filterable", "address"));
            }
        }
        if(postalCode != null && !postalCode.trim().equalsIgnoreCase("")){
            postalCode = postalCode.replaceAll("[-+.^:,]","");
            tags.add(new Tag(postalCode, "filterable", "address"));
        }
        if(country != null && !country.trim().equalsIgnoreCase("")){
            country = country.replaceAll("[-+.^:,]","");
            tags.add(new Tag(country, "filterable", "address"));
        }
        return tags;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setSubLocality(String subLocality) {
        this.subLocality = subLocality;
    }

    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }

    public void setSubAdminArea(String subAdminArea) {
        this.subAdminArea = subAdminArea;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public void setPremises(String premises) {
        this.premises = premises;
    }

    public void setSubThoroughFare(String subThoroughFare) {
        this.subThoroughFare = subThoroughFare;
    }

    public void setThoroughFare(String thoroughFare) {
        this.thoroughFare = thoroughFare;
    }

    public String getStorable() {
        return this.storable;
    }

    public void setStorable(String storable) {
        this.storable = storable;
    }

    public String getDisplayable() {
        return this.displayable;
    }

    public void setDisplayable(String displayable) {
        this.displayable = displayable;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this).toString();
    }
}
