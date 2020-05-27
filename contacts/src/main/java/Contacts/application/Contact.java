package Contacts.application;


import java.util.ArrayList;
import java.util.List;

public class Contact {

    private int id;
    private String surname;
    private String name;
    private String number;
    private String email;
    private List<String> books = new ArrayList<String>();

    public Contact(){}

    public Contact(Integer id, String surname, String name, String number, String email){
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.number = number;
        this.email = email;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setSurname(String surname){
        this.surname = surname;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public void setEmail(String email){

        this.email = email;
    }

    public int getId(){
        return id;
    }

    public String getSurname(){
        return surname;
    }

    public String getName(){
        return name;
    }

    public String getNumber(){
        return number;
    }

    public String getEmail(){
        return email;
    }

    public List<String> getBooks(){
        return books;
    }

    public void addBook(String book){
        books.add(book);
    }

    public void setBooks(List<String> bookList){this.books = bookList;}

    public int delBook(String book){
        for(String str : books) {
            if (str.equals(book)) {
                books.remove(str);
                return 1;
            }
        }
        return 2;
    }



    public boolean equals(Contact contact){
        if(contact == null) return false;
        else{
            if(surname.equals(contact.getSurname()) && name.equals(contact.getName()) && number.equals(contact.getNumber())) return true;
        }
        return false;
    }

}
