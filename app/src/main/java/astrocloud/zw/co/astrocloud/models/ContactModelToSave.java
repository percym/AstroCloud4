package astrocloud.zw.co.astrocloud.models;

/**
 * Created by Percy M on 6/27/2016.
 */
public class ContactModelToSave {
    public String name, number, userId;

    public ContactModelToSave(){

    }
    public ContactModelToSave(String name, String number , String userId) {

        this.name = name;
        this.number = number;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
