package Contacts.application;

import com.google.gson.Gson;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class Access {

    private static ArrayList<Contact> contactList;

    public static void initContacts() {
        RestTemplate t = new RestTemplate();

        contactList = new ArrayList<Contact>();
        contactList.add(new Contact(12345, "Vangogh", "Jake", "+37065841738", "jakevan@mail.com"));
        contactList.get(0).addBook("9786098233254");
        contactList.add(new Contact(74638, "Dirk", "Mike", "+37064787734", "mikedirk@mail.com"));
        contactList.get(1).addBook("9786090138823");
        contactList.add(new Contact(87014, "Davis", "Luke", "+37064787735", "davisluke@mail.com"));
        contactList.get(2).addBook("9786094840425");
        contactList.add(new Contact(11234, "Mer", "Eva", "+37064787737", "EvaMer@mail.com"));

        initBooks();


    }

    public static void initBooks() {

        RestTemplate t = new RestTemplate();

        //Gson gson = new Gson();
        JSONObject convertedObject;

        String json= "{\"Pavadinimas\":\"Metai\",\"Autorius\":\"Kristijonas Donelaitis\",\"ISBN\":\"1786554684\",\"Metai\":\"1818\"}";

        convertedObject = new Gson().fromJson(json, JSONObject.class);
        t.postForEntity("http://library:80/books", convertedObject, String.class);
        contactList.get(0).addBook("1786554684");

    }


    public List<Contact> getAllContacts() {
        if(contactList == null)
            initContacts();

        return contactList;
    }

    public Contact getContact(int id) {
        if(contactList == null)
            initContacts();
        for(Contact contact : contactList){
            if(contact.getId() == id) return contact;
        }
        return null;
    }

    public int addContact(Contact nContact) {
        if(contactList == null)
            initContacts();

        for (Contact contact : contactList) {
            if (contact.equals(nContact) || contact.getId() == nContact.getId() || contact.getNumber() == nContact.getName()) {
                return 0;
            }

        }

        if (nContact.getId() == 0 || nContact.getName() == null || nContact.getNumber() == null || nContact.getSurname() == null || nContact.getEmail() == null) {
            return 2;
        }

        if (!nContact.getBooks().isEmpty()) {
            return 3;
        }

        contactList.add(nContact);
        return 1;
    }


    public int updateContact(int oId, Contact uContact){

        if(contactList == null)
            initContacts();

        if (uContact.getName() == null || uContact.getNumber() == null || uContact.getSurname() == null || uContact.getEmail() == null) {
            return 2;
        }

        if (!uContact.getBooks().isEmpty()) {
            return 4;
        }

        for(Contact contact : contactList) {
            if(contact.getId() == uContact.getId()){
                return 3;
            }
        }

        if(uContact.getId() == 0)
            uContact.setId(oId);
        for(Contact contact : contactList){
            if(contact.getId() == oId){
                int index = contactList.indexOf(contact);
                contactList.set(index, uContact);
                return 1;
            }
        }

        return 0;
    }

    public int deleteContact(int id) {

        if(contactList == null)
            initContacts();

        RestTemplate t = new RestTemplate();
        JSONObject book = new JSONObject();

        HttpEntity<JSONObject> entity = new HttpEntity(book);

        for(Contact contact : contactList){
            if(contact.getId() == id){
                for(String str : contact.getBooks())
                {
                    t.exchange("http://library:80/books/" + str, HttpMethod.DELETE, entity, String.class);
                }
                contactList.remove(contact);
                return 1;
            }
        }
        return 0;
    }


    public int deleteBook(int id, String isbn){

        if(contactList == null)
            initContacts();

        for(Contact contact : contactList) {
            if (contact.getId() == id) {
                 return contact.delBook(isbn);
                }
            }
        return 0;
    }


}
