package com.github.fedorovr.xxe;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

@RestController
public class MainController {
  public static final int TOKEN_SIZE = 128;
  private final Map<String, Integer> userIdByToken = new HashMap<>();
  private final Map<String, Integer> idByLogin = new HashMap<>();
  private final Map<Integer, String> passwordsByUserId = new HashMap<>();
  private final Map<Integer, List<Wallet>> walletsByUserId = new HashMap<>();
  private final String tokenChars = "QWERTYUIOPLKJHGFDSAZXCVBNM1234567890";

  private String generateRandomString(int size) {
    char[] array = new char[size];
    for (int i = 0; i < array.length; i++) {
      array[i] = tokenChars.charAt(new Random().nextInt(tokenChars.length()));
    }
    return new String(array);
  }

  public MainController() {
    idByLogin.put("login", 1);
    passwordsByUserId.put(1, "pass");
    walletsByUserId.put(1, new ArrayList<>(Arrays.asList(new Wallet("name1", "public1", "secret1"),
      new Wallet("name2", "public2", "secret2"))));
  }

  @RequestMapping("/resource")
  public Map<String, Object> home() {
    Map<String, Object> model = new HashMap<>();
    model.put("id", UUID.randomUUID().toString());
    model.put("content", "Hello World");
    return model;
  }

  @PostMapping(value = "/login")
  public Map<Object, Object> login(@RequestParam(value = "login") String login, @RequestParam(value = "password") String password) {
    Map<Object, Object> model = new HashMap<>();
    if (idByLogin.containsKey(login.toLowerCase())) {
      int userId = idByLogin.get(login.toLowerCase());
      if (passwordsByUserId.get(userId).equals(password)) {
        String token = generateRandomString(TOKEN_SIZE);
        model.put("token", token);
        userIdByToken.put(token, userId);
      }
    }
    return model;
  }

  @PostMapping("/getWallets")
  public Map<String, Object> getWallets(@RequestParam(value = "token") String token) {
    Map<String, Object> model = new HashMap<>();
    if (userIdByToken.containsKey(token)) {
      model.put("wallets", walletsByUserId.get(userIdByToken.get(token)));
    }
    return model;
  }

  @PostMapping("/createWallet")
  public Map<String, Object> createWallet(@RequestParam(value = "token") String token, @RequestParam(value = "name") String name) {
    Map<String, Object> model = new HashMap<>();
    if (userIdByToken.containsKey(token)) {
      List<Wallet> wallets = walletsByUserId.getOrDefault(userIdByToken.get(token), new ArrayList<>());
      wallets.add(new Wallet(name, generateRandomString(20), generateRandomString(100)));
      model.put("wallets", wallets);
    }
    return model;
  }

  @PostMapping("/exportWallets")
  @ResponseBody
  public FileSystemResource exportWallets(@RequestParam(value = "token") String token, HttpServletResponse response) throws URISyntaxException {
    Map<String, Object> model = new HashMap<>();
    if (userIdByToken.containsKey(token)) {
      int userId = userIdByToken.get(token);
      String location = String.format("C:\\Users\\Elena\\IdeaProjects\\xxe\\src\\main\\resources\\xxe-%d.xml", userId);
      saveToXML(walletsByUserId.getOrDefault(userId, Collections.emptyList()), location);

      File fXmlFile = new File(location);
      response.setContentType("application/xml");
      return new FileSystemResource(fXmlFile);
    }
    return null;
  }

  private static void saveToXML(List<Wallet> wallets, String xml) {
    Document dom;

    // instance of a DocumentBuilderFactory
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      // use factory to get an instance of document builder
      DocumentBuilder db = dbf.newDocumentBuilder();
      // create instance of DOM
      dom = db.newDocument();

      // create the root element
      Element templatesRoot = dom.createElement("templates");

      for (Wallet wallet : wallets) {
        // create data elements and place them under root
        Element template = dom.createElement("template");

        Element name = dom.createElement("name");
        name.appendChild(dom.createTextNode(wallet.getName()));
        template.appendChild(name);

        Element publicKey = dom.createElement("publicKey");
        publicKey.appendChild(dom.createTextNode(wallet.getPublicKey()));
        template.appendChild(publicKey);

        Element privateKey = dom.createElement("privateKey");
        privateKey.appendChild(dom.createTextNode(wallet.getPrivateKey()));
        template.appendChild(privateKey);

        templatesRoot.appendChild(template);
      }
      dom.appendChild(templatesRoot);

      try {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // send DOM to file
        tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(xml)));

      } catch (TransformerException te) {
        System.out.println(te.getMessage());
      } catch (IOException ioe) {
        System.out.println(ioe.getMessage());
      }
    } catch (ParserConfigurationException pce) {
      System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
    }
  }

  @PostMapping("/importWallets")
  public Map<String, Object> importWallets(@RequestParam(value = "token") String token, @RequestParam("file") MultipartFile multipartFile) throws IOException {
    Map<String, Object> model = new HashMap<>();
    if (userIdByToken.containsKey(token)) {
      int userId = userIdByToken.get(token);
      List<Wallet> wallets = walletsByUserId.getOrDefault(userId, new ArrayList<>());
      List<Wallet> importedWallets = readWallets(multipartFile.getInputStream());
      wallets.addAll(importedWallets);
    }
    return getWallets(token);
  }

  public static List<Wallet> readWallets(InputStream inputStream) {
    List<Wallet> wallets = new ArrayList<>();
    try {
      // First, create a new XMLInputFactory
      XMLInputFactory inputFactory = XMLInputFactory.newInstance();
      // Setup a new eventReader
      InputStream in = inputStream;
      XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
      // read the XML document
      Wallet wallet = new Wallet();

      while (eventReader.hasNext()) {
        XMLEvent event = eventReader.nextEvent();

        if (event.isStartElement()) {
          StartElement startElement = event.asStartElement();
          if (startElement.getName().getLocalPart().equals("wallet")) {
            wallet = new Wallet();
          }

          if (event.isStartElement()) {
            if (event.asStartElement().getName().getLocalPart().equals("name")) {
              event = eventReader.nextEvent();
              wallet.setName(event.asCharacters().getData());
              continue;
            }
          }
          if (event.asStartElement().getName().getLocalPart().equals("publicKey")) {
            event = eventReader.nextEvent();
            wallet.setPublicKey(event.asCharacters().getData());
            continue;
          }
          if (event.asStartElement().getName().getLocalPart().equals("privateKey")) {
            event = eventReader.nextEvent();
            wallet.setPrivateKey(event.asCharacters().getData());
            continue;
          }


        }
        // If we reach the end of an item element, we add it to the list
        if (event.isEndElement()) {
          EndElement endElement = event.asEndElement();
          if (endElement.getName().getLocalPart().equals("wallet")) {
            wallets.add(wallet);
          }
        }

      }
    } catch (XMLStreamException e) {
      e.printStackTrace();
    }
    return wallets;
  }
}
