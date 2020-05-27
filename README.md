# contacts-soap
Web service with contacts and a list of their borrowed books from library

1. Clone git repository: git clone ```https://github.com/augkik/contacts-2.git```
2. Launch Web service: ```docker-compose up```
Instructions:

GET

List of all contacts:
```<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:getContactsRequest>
         <con:expand>?</con:expand>
      </con:getContactsRequest>
   </soapenv:Body>
</soapenv:Envelope>```

Contact with particular id: /contacts/<id>

List of particular contact books: /contacts/<id>/books

List of all contacts with books: /contacts?expand=books

PUT

Update particular contact: /contacts/<id>

Update particular contact book: /contacts/<id>/books/<isbn>

DELETE

Remove particular contact: /contacts/<id>

Remove particular contact book: /contacts/<id>/books/<isbn>

POST

Add new contact: /contacts body example: {"number": "", "surname": "", "name": "", "id": ,"email": "", "book": {"Pavadinimas": "", "Autorius": "","ISBN": "", "Metai":}}

Add new book: /contacts/<id>/books
