# contacts-soap
Web service with contacts and a list of their borrowed books from library

1. Clone git repository: git clone ```https://github.com/augkik/contacts-soap.git```
2. Launch Web service: ```docker-compose up```
Instructions:

WSDL file:
```/SoapContacts/contacts.wsdl```<br />
Requests:
```/SoapContacts```

### Read

List of all contacts:
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:getContactsRequest>
         <con:expand>?</con:expand>
      </con:getContactsRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

Contact with particular id:
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:getContactRequest>
         <con:id>?</con:id>
      </con:getContactRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

List of particular contact books:
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:getContactBooksRequest>
         <con:id>?</con:id>
      </con:getContactBooksRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

### Create

Add new contact (with or without book):
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:addContactRequest>
         <con:contact>
            <con:id>?</con:id>
            <con:name>?</con:name>
            <con:surname>?</con:surname>
            <con:number>?</con:number>
            <con:email>?</con:email>
         </con:contact>
         <con:book>
            <con:isbn>?</con:isbn>
            <con:title>?</con:title>
            <con:author>?</con:author>
            <con:year>?</con:year>
         </con:book>
      </con:addContactRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

Add new contact book:
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:addBookRequest>
         <con:id>?</con:id>
         <con:book>
            <con:isbn>?</con:isbn>
            <con:title>?</con:title>
            <con:author>?</con:author>
            <con:year>?</con:year>
         </con:book>
      </con:addBookRequest>
   </soapenv:Body>
</soapenv:Envelope>
```
### Remove

Remove particular contact:
```<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:deleteContactRequest>
         <con:id>?</con:id>
      </con:deleteContactRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

Remove particular contact book:
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:deleteBookRequest>
         <con:id>?</con:id>
         <con:isbn>?</con:isbn>
      </con:deleteBookRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

### Update

Update book:
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:updateBookRequest>
         <con:id>?</con:id>
         <con:book>
            <con:isbn>?</con:isbn>
            <con:title>?</con:title>
            <con:author>?</con:author>
            <con:year>?</con:year>
         </con:book>
      </con:updateBookRequest>
   </soapenv:Body>
</soapenv:Envelope>
```
Update contact:
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:con="http://www.example.com/contacts">
   <soapenv:Header/>
   <soapenv:Body>
      <con:updateContactRequest>
         <con:id>?</con:id>
         <con:contact>
            <con:id>?</con:id>
            <con:name>?</con:name>
            <con:surname>?</con:surname>
            <con:number>?</con:number>
            <con:email>?</con:email>
         </con:contact>
      </con:updateContactRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

