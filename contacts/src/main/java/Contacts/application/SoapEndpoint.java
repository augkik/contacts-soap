package Contacts.application;

import com.example.contacts.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;


@Endpoint
public class SoapEndpoint {

    Access access = new Access();
    private static final String URI = "http://www.example.com/contacts";

    // GET ALL contacts with/without books
    @PayloadRoot(namespace = URI, localPart = "getContactsRequest")
    @ResponsePayload
    public GetContactsResponse getContacts(@RequestPayload GetContactsRequest request) throws JsonProcessingException {
        GetContactsResponse response = new GetContactsResponse();
        List<Contact> contactList = access.getAllContacts();

        if (request.getExpand() != null && request.getExpand().equals("books")) {
            for (Contact contact : contactList) {
                ContactWithBooks c = new ContactWithBooks();
                c.setId(contact.getId());
                c.setName(contact.getName());
                c.setSurname(contact.getSurname());
                c.setEmail(contact.getEmail());
                c.setNumber(contact.getNumber());
                GetContactBooksRequest req = new GetContactBooksRequest();
                req.setId(contact.getId());
                GetContactBooksResponse books = getContactBooks(req); //list of contact books
                for (Book b : books.getBook()) {
                    c.getBooks().add(b);
                }
                response.getContactWithBooks().add(c);
            }

        } else {
            if (!contactList.isEmpty()) {
                for (Contact contact : contactList) {
                    ContactOnly c = new ContactOnly();
                    c.setId(contact.getId());
                    c.setName(contact.getName());
                    c.setSurname(contact.getSurname());
                    c.setEmail(contact.getEmail());
                    c.setNumber(contact.getNumber());
                    response.getContact().add(c);

                }
            }
        }
        return response;
    }

    // GET ONE contact without books (should be with books)
    @PayloadRoot(namespace = URI, localPart = "getContactRequest")
    @ResponsePayload
    public GetContactResponse getContact(@RequestPayload GetContactRequest request) {
        GetContactResponse response = new GetContactResponse();
        ContactWithBooks c = new ContactWithBooks();
        Contact contact = access.getContact(request.getId());
        if (contact == null) return response;
        c.setId(contact.getId());
        c.setName(contact.getName());
        c.setSurname(contact.getSurname());
        c.setEmail(contact.getEmail());
        c.setNumber(contact.getNumber());
        response.setContact(c);
        return response;
    }

    // GET ONE contact books
    @PayloadRoot(namespace = URI, localPart = "getContactBooksRequest")
    @ResponsePayload
    public GetContactBooksResponse getContactBooks(@RequestPayload GetContactBooksRequest request) throws JsonProcessingException {
        GetContactBooksResponse response = new GetContactBooksResponse();
        int id = request.getId();

        Contact contact = access.getContact(id);

        if (contact == null) return response;

        RestTemplate t = new RestTemplate();
        ResponseEntity<String> res;
        JSONObject obj = new JSONObject();
        HttpEntity<JSONObject> entity = new HttpEntity(obj);
        ObjectMapper mapper = new ObjectMapper();
        ;

        String bookStr;
        Gson gson = new Gson();
        JSONObject convertedObject;
        System.out.println(contact.getBooks());

        for (String str : contact.getBooks()) {
            res = t.exchange("http://library:80/books/" + str, HttpMethod.GET, entity, String.class);
            bookStr = res.getBody();
            convertedObject = gson.fromJson(bookStr, JSONObject.class);

            String tempStr = mapper.writeValueAsString(convertedObject.get("Knyga"));
            JSONObject bookJson = gson.fromJson(tempStr, JSONObject.class);

            Book book = new Book();
            String isbn = (String) bookJson.get("ISBN");
            book.setIsbn(isbn);
            String title = (String) bookJson.get("Pavadinimas");
            ;
            book.setTitle(title);
            String author = (String) bookJson.get("Autorius");
            book.setAuthor(author);
            double y = (double) bookJson.get("Metai");

            int year = (int) y;

            book.setYear(year);

            response.getBook().add(book);

        }
        return response;
    }

    // POST ONE contact

    @PayloadRoot(namespace = URI, localPart = "addContactRequest")
    @ResponsePayload
    public AddContactResponse addContact(@RequestPayload AddContactRequest request) {
        System.out.println("Funkcija:1");
        AddContactResponse response = new AddContactResponse();
        ContactOnly c = request.getContact();
        Book book = request.getBook();
        //create and add contact to the list
        Contact contact = new Contact(c.getId(), c.getSurname(), c.getName(), c.getNumber(), c.getEmail());
        //contact.addBook(book.getIsbn());
        int res = access.addContact(contact);
        //iff contact was added successfully, add book

        System.out.println("Funkcija:2");
        if (book != null && res == 1) {
            JSONObject bookJson = new JSONObject();
            bookJson.put("ISBN", book.getIsbn());
            bookJson.put("Autorius", book.getAuthor());
            bookJson.put("Pavadinimas", book.getTitle());
            bookJson.put("Metai", book.getYear());

            //post a book
            RestTemplate t = new RestTemplate();
            HttpEntity<JSONObject> entity = new HttpEntity(bookJson);
            access.deleteContact(c.getId());
            System.out.println("Atspausdina: ");
            System.out.println("Metai: " + book.getYear() + " ISBN: " + book.getIsbn() + " Autorius: " + book.getAuthor() + " Pavadinimas: " + book.getTitle());
            System.out.println("Atspausdinta.");
            // if(book.getYear() != 0)

            boolean bookData = true;
            if (book.getIsbn().equals("?") || book.getTitle().equals("?") || book.getAuthor().equals("?") || book.getYear() == 0) {
                bookData = false;
            }

            if (bookData) {
                ResponseEntity<String> r = t.exchange("http://library:80/books", HttpMethod.POST, entity, String.class);
                if (r.getStatusCodeValue() == 201) {
                    access.addContact(contact);
                    contact.addBook(book.getIsbn());
                    response.setResponse("Book added successfully. Contact added successfully.");
                } else {
                    response.setResponse("Failed. Could not add book.");
                }

            } else {
                response.setResponse("Failed. Could not add book.");
            }
            return response;
        }

        if (res == 1) {
            response.setResponse("Contact added successfully.");
            return response;
        } else if (res == 2) {
            response.setResponse("Failed. Wrong data.");
            return response;
        } else if (res == 3) {
            response.setResponse("Failed. Cannot add books.");
            return response;
        } else {
            response.setResponse("Failed. Contact already exists.");
            return response;
        }
    }

    // POST a book

    @PayloadRoot(namespace = URI, localPart = "addBookRequest")
    @ResponsePayload
    public AddBookResponse addBook(@RequestPayload AddBookRequest request) {
        AddBookResponse response = new AddBookResponse();
        Book b = request.getBook();
        JSONObject book = new JSONObject();

        book.put("ISBN", b.getIsbn());
        book.put("Autorius", b.getTitle());
        book.put("Pavadinimas", b.getTitle());
        book.put("Metai", b.getYear());


        List<Contact> list = access.getAllContacts();

        boolean bookData = true;
        if (b.getIsbn().equals("?") || b.getTitle().equals("?") || b.getAuthor().equals("?") || b.getYear() == 0) {
            bookData = false;
        }

        if (access.getContact(request.getId()) != null) {
            if (bookData) {
                RestTemplate t = new RestTemplate();
                HttpEntity<JSONObject> entity = new HttpEntity(book);
                ResponseEntity<String> res = t.exchange("http://library:80/books", HttpMethod.POST, entity, String.class);
                if (res.getStatusCodeValue() == 201) {

                    String isbn = book.get("ISBN").toString();
                    for (Contact contact : list) {
                        if (contact.getId() == request.getId()) {
                            contact.addBook(isbn);
                        }
                    }
                } else {
                    response.setResponse("Failed. Wrong data.");
                    return response;
                }
                response.setResponse("Book added successfully.");
            } else {
                response.setResponse("Failed. Could not add book.");
            }
        } else {
            response.setResponse("Failed Could not find contact.");
        }
        return response;
    }

    // DELETE ONE contact
    @PayloadRoot(namespace = URI, localPart = "deleteContactRequest")
    @ResponsePayload
    public DeleteContactResponse deleteContact(@RequestPayload DeleteContactRequest request) {
        DeleteContactResponse response = new DeleteContactResponse();
        int res = access.deleteContact(request.getId());
        if (res == 1) {
            response.setResponse("Contact deleted successfully.");
            return response;
        }
        response.setResponse("Failed. Could not find contact.");
        return response;
    }

    // DELETE a book
    @PayloadRoot(namespace = URI, localPart = "deleteBookRequest")
    @ResponsePayload
    public DeleteBookResponse deleteBook(@RequestPayload DeleteBookRequest request) {
        DeleteBookResponse response = new DeleteBookResponse();

        RestTemplate t = new RestTemplate();
        JSONObject book = new JSONObject();
        HttpEntity<JSONObject> entity = new HttpEntity(book);
        String isbn = request.getIsbn();

        int res = access.deleteBook(request.getId(), isbn);
        if (res == 1) {
            t.exchange("http://library:80/books/" + isbn, HttpMethod.DELETE, entity, String.class);
            response.setResponse("Book deleted successfully.");
            return response;
        } else if (res == 2) {
            response.setResponse("Failed. Could not find book.");
            return response;
        } else {
            response.setResponse("Failed. Could not find contact.");
            return response;
        }
    }

    // UPDATE a contact
    @PayloadRoot(namespace = URI, localPart = "updateContactRequest")
    @ResponsePayload
    public UpdateContactResponse updateContact(@RequestPayload UpdateContactRequest request) {
        UpdateContactResponse response = new UpdateContactResponse();
        int oldId = request.getId();
        ContactOnly c = request.getContact();
        Contact uContact = new Contact(c.getId(), c.getSurname(), c.getName(), c.getNumber(), c.getEmail());

        List<String> booksStr = access.getContact(oldId).getBooks();

        if (access.getContact(oldId) != null) {

            int res = access.updateContact(oldId, uContact);

            if (uContact.getId() == 0) {
                access.getContact(oldId);
            } else {
                access.getContact(uContact.getId());
            }
            if (res == 1) {
                uContact.setBooks(booksStr);
                response.setResponse("Contact updated successfully.");
                return response;
            } else if (res == 3) {
                response.setResponse("Failed. New id already exists.");
                return response;
            } else if (res == 4) {
                response.setResponse("Failed. Cannot update books.");
                return response;
            } else {
                response.setResponse("Failed. Wrong data.");
                return response;
            }

        } else {
            response.setResponse("Failed. Could not find contact.");
            return response;
        }

    }

    // UPDATE a book
    @PayloadRoot(namespace = URI, localPart = "updateBookRequest")
    @ResponsePayload
    public UpdateBookResponse updateBook(@RequestPayload UpdateBookRequest request) {
        UpdateBookResponse response = new UpdateBookResponse();
        int oid = request.getId();
        Book b = request.getBook();
        String isbn = b.getIsbn();
        JSONObject book = new JSONObject();
        book.put("Autorius", b.getAuthor());
        book.put("Padavinimas", b.getTitle());
        book.put("Metai", b.getYear());

        //List<String> booksStr = access.getContact(oldId).getBooks();
        List<Contact> list = access.getAllContacts();

        if (access.getContact(oid) != null) {
            RestTemplate t = new RestTemplate();
            HttpEntity<JSONObject> entity = new HttpEntity(book);
            ResponseEntity<String> res = t.exchange("http://library:80/books/" + isbn, HttpMethod.PUT, entity, String.class);
            if (res.getStatusCodeValue() == 200) {
                response.setResponse("Contact updated successfully.");
            } else {
                response.setResponse("Failed. Wrong data.");
            }
        } else {
            response.setResponse("Failed Could not find contact.");
        }
        return response;

    }

}
