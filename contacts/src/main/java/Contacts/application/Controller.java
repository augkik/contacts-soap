package Contacts.application;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.json.simple.JSONObject;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;

@RestController
public class Controller {
    Access contactAccess = new Access();

    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    public ResponseEntity<List<Contact>> getContacts(){
        List<Contact> contactList = contactAccess.getAllContacts();
        if(contactList.isEmpty()) return new ResponseEntity<List<Contact>>(contactList, HttpStatus.NO_CONTENT);
        else return new ResponseEntity<List<Contact>>(contactList, HttpStatus.OK);
    }

    @RequestMapping(value = "/contacts/{id}", method = RequestMethod.GET)
    public ResponseEntity<Contact> getContact(@PathVariable(value="id") int id){
        Contact contact = contactAccess.getContact(id);
        if(contact == null) return new ResponseEntity<Contact>(contact, HttpStatus.NOT_FOUND);
        return new ResponseEntity<Contact>(contact, HttpStatus.OK);
    }

    @RequestMapping(value = "/contacts/{id}/books", method = RequestMethod.GET)
    public ResponseEntity<List<JSONObject>> getBooks(@PathVariable(value="id") int id) throws JsonProcessingException {
        Contact contact = contactAccess.getContact(id);
        JSONObject obj = new JSONObject();
        List<JSONObject> books = new ArrayList<JSONObject>();
        if(contact == null) return new ResponseEntity<List<JSONObject>>(books, HttpStatus.NOT_FOUND);
        RestTemplate t = new RestTemplate();
        ResponseEntity<String> response;
        HttpEntity<JSONObject> entity = new HttpEntity(obj);
        Gson gson = new Gson();
        String json;
        JSONObject convertedObject; //= new JSONObject();
        for(String str : contact.getBooks()){
            response = t.exchange("http://library:80/books/" + str, HttpMethod.GET, entity, String.class);

            json = response.getBody();
            convertedObject = new Gson().fromJson(json, JSONObject.class);
            books.add(convertedObject);
        }

        return new ResponseEntity<List<JSONObject>>(books, HttpStatus.OK);
    }

    @RequestMapping(value = "/contacts",params  = "expand", method = RequestMethod.GET)
    public ResponseEntity<List<JSONObject>> getAllBooks(@RequestParam("expand") String expand) throws JsonProcessingException{
        List<JSONObject> books = new ArrayList<JSONObject>();
        List<JSONObject> all = new ArrayList<JSONObject>();
        JSONObject convertedObject;

        ObjectMapper mapper = new ObjectMapper();
        if(expand.equals("books")) {

            List<Contact> contactList = contactAccess.getAllContacts();
            List<JSONObject> list = new ArrayList<JSONObject>();
            for (Contact contact : contactList) {
                books = getBooks(contact.getId()).getBody();

                String like = mapper.writeValueAsString(contact);


                JSONObject obj = new JSONObject();
                //JSONObject obj = new Gson().fromJson(like, JSONObject.class);
                obj.put("name", contact.getName());
                obj.put("number", contact.getNumber());
                obj.put("id", contact.getId());
                obj.put("surname", contact.getSurname());
                obj.put("email", contact.getEmail());
                obj.put("books", books);

                all.add(obj);
            }
            return new ResponseEntity<List<JSONObject>>(all, HttpStatus.OK);
        }
        else return new ResponseEntity<List<JSONObject>>(all, HttpStatus.BAD_REQUEST);
    }


    @RequestMapping(value = "/contact", method = RequestMethod.POST)
    public ResponseEntity<String> addContact(@Valid @RequestBody Contact contact, UriComponentsBuilder b){
        int response = contactAccess.addContact(contact);
        List<Contact> list = contactAccess.getAllContacts();
        Contact element = list.get(list.size() - 1);
        HttpHeaders headers = headerBuilder(b, element.getId());

        if(response == 1) return new ResponseEntity<String>("Contact added successfully.", headers, HttpStatus.CREATED);
        else if(response == 2) return new ResponseEntity<String>("Failed. Wrong data.", headers, HttpStatus.BAD_REQUEST);
        else if(response == 3) return new ResponseEntity<String>("Failed. Cannot add books.", headers, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<String>("Failed. Contact already exists.", headers, HttpStatus.BAD_REQUEST);


    }

    @RequestMapping(value = "/contacts", method = RequestMethod.POST)
    public ResponseEntity<String> addCon(@Valid @RequestBody JSONObject jsonO, UriComponentsBuilder b) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String name = (String) jsonO.get("name");
        String surname = (String) jsonO.get("surname");
        String number = (String) jsonO.get("number");
        String email = (String) jsonO.get("email");
        int id  = (int) jsonO.get("id");
        Contact contact =  new Contact(id,surname,name,number,email);
        int res = contactAccess.addContact(contact); // adding contact

        List<Contact> list = contactAccess.getAllContacts();
        Contact element = list.get(list.size() - 1);
        HttpHeaders headers = headerBuilder(b, element.getId());


        Object book = jsonO.get("book");
        if(book != null && res == 1) {

            String like = mapper.writeValueAsString(book);
            JSONObject bookJson = new Gson().fromJson(like, JSONObject.class);

            String isbn = (String) bookJson.get("ISBN");
            double met = (double) bookJson.get("Metai");
            int metai = (int) met;

            bookJson.put("Metai", metai);

            RestTemplate t = new RestTemplate();
            HttpEntity<JSONObject> entity = new HttpEntity(bookJson);
            deleteContact(id);
            ResponseEntity<String> response = t.exchange("http://library:80/books", HttpMethod.POST, entity, String.class);
            if(response.getStatusCodeValue() == 201) {
                contactAccess.addContact(contact);
                contact.addBook(isbn);
                return new ResponseEntity<String>("Book added successfully. Contact added successfully.", headers, HttpStatus.CREATED);

            }
            else {
                return new ResponseEntity<String>("Failed. Could not add book.", headers, HttpStatus.BAD_REQUEST);

            }
        }


        if(res == 1 ) return new ResponseEntity<String>("Contact added successfully.", headers, HttpStatus.CREATED);
        else if(res == 2) return new ResponseEntity<String>("Failed. Wrong data.", headers, HttpStatus.BAD_REQUEST);
        else if(res == 3) return new ResponseEntity<String>("Failed. Cannot add books.", headers, HttpStatus.BAD_REQUEST);
        else return new ResponseEntity<String>("Failed. Contact already exists.", HttpStatus.BAD_REQUEST);

    }

    @RequestMapping(value = "/contacts/{id}/books", method = RequestMethod.POST)
    public ResponseEntity<String> addBook(@PathVariable(value="id") int id, @Valid @RequestBody JSONObject book, UriComponentsBuilder b) throws JsonProcessingException {

        List<Contact> list = contactAccess.getAllContacts();
        if(contactAccess.getContact(id) != null){
            RestTemplate t = new RestTemplate();
            HttpEntity<JSONObject> entity = new HttpEntity(book);
            ResponseEntity<String> response = t.exchange("http://library:80/books", HttpMethod.POST, entity, String.class);
            if(response.getStatusCodeValue() == 201){

                String isbn = book.get("ISBN").toString();

                for(Contact contact : list){
                    if(contact.getId() == id){
                        contact.addBook(isbn);
                    }
                }
            }
            else return new ResponseEntity<String>("Failed. Wrong data.", HttpStatus.BAD_REQUEST);
            HttpHeaders headers = headerBuilder(b, contactAccess.getContact(id).getId());
            return new ResponseEntity<String>("Book added successfully.", headers, HttpStatus.OK);
        }
        else return new ResponseEntity<String>("Failed Could not find contact.", HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/contacts/{id}", method = RequestMethod.PUT)
    public ResponseEntity<String> updateContact(@PathVariable(value="id") int oid, @Valid @RequestBody Contact contact, UriComponentsBuilder b){
        List<String> booksStr = contactAccess.getContact(oid).getBooks();

        if(contactAccess.getContact(oid) != null){


            int response = contactAccess.updateContact(oid, contact);
            Contact element;
            HttpHeaders headers;

            if(contact.getId() == 0){
                element = contactAccess.getContact(oid);
            }
            else{
                element = contactAccess.getContact(contact.getId());
            }
            headers = headerBuilder(b, element.getId());
            if(response == 1) {
                contact.setBooks(booksStr);
                return new ResponseEntity<String>("Contact updated successfully.", headers, HttpStatus.OK);
            }
            else if(response == 3)
                return new ResponseEntity<String>("Failed. New id already exists.", headers, HttpStatus.BAD_REQUEST);
            else if (response == 4)
                return new ResponseEntity<String>("Failed. Cannot update books.", headers, HttpStatus.BAD_REQUEST);
            else
                return new ResponseEntity<String>("Failed. Wrong data.", headers, HttpStatus.BAD_REQUEST);

        }
        else return new ResponseEntity<String>("Failed. Could not find contact.", HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/contacts/{id}/books/{isbn}", method = RequestMethod.PUT)
    public ResponseEntity<String> updateBook(@PathVariable(value="id") int oid, @PathVariable(value="isbn") int isbn, @Valid @RequestBody JSONObject book, UriComponentsBuilder b) throws JsonProcessingException {
        List<Contact> list = contactAccess.getAllContacts();
        if(contactAccess.getContact(oid) != null){
            RestTemplate t = new RestTemplate();
            HttpEntity<JSONObject> entity = new HttpEntity(book);
            ResponseEntity<String> response = t.exchange("http://library:80/books/" + isbn, HttpMethod.PUT, entity, String.class);
            if(response.getStatusCodeValue() == 200){
                HttpHeaders headers = headerBuilder(b, contactAccess.getContact(oid).getId());
                return new ResponseEntity<String>("Book updated successfully.", headers, HttpStatus.OK);
                }
            else return new ResponseEntity<String>("Failed. Wrong data.", HttpStatus.BAD_REQUEST);
        }
        else return new ResponseEntity<String>("Failed Could not find contact.", HttpStatus.NOT_FOUND);
    }


    @RequestMapping(value = "/contacts/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteContact(@PathVariable(value="id") int id){
        int response = contactAccess.deleteContact(id);
        if(response == 1) return new ResponseEntity<String>("Contact deleted successfully.", HttpStatus.OK);
        return new ResponseEntity<String>("Failed. Could not find contact.", HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/contacts/{id}/books/{isbn}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteBook(@PathVariable(value="id") int id, @PathVariable(value="isbn") String isbn){
        RestTemplate t = new RestTemplate();
        JSONObject book = new JSONObject();
        HttpEntity<JSONObject> entity = new HttpEntity(book);
        int response = contactAccess.deleteBook(id,isbn);
        if(response == 1) {
            t.exchange("http://library:80/books/" + isbn, HttpMethod.DELETE, entity, String.class);
            return new ResponseEntity<String>("Book deleted successfully.", HttpStatus.OK);
        }
        else if (response == 2) return new ResponseEntity<String>("Failed. Could not find book.", HttpStatus.NOT_FOUND);
        else return new ResponseEntity<String>("Failed. Could not find contact.", HttpStatus.NOT_FOUND);
    }

    public HttpHeaders headerBuilder(UriComponentsBuilder b, int id){
        UriComponents uriComponents = b.path("/contacts/{id}").buildAndExpand(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return headers;
    }
}
