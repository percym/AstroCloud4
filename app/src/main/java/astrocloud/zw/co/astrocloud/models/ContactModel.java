package astrocloud.zw.co.astrocloud.models;

/**
 * Created by Percy M on 6/27/2016.
 */
public class ContactModel {
    public String name, number;

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
    public ContactModel(){

    }
    public ContactModel(String name, String number) {

        this.name = name;
        this.number = number;
    }
}
