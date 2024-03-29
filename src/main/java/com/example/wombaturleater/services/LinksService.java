package com.example.wombaturleater.services;

import com.example.wombaturleater.entities.Link;
import com.example.wombaturleater.entities.Person;
import com.example.wombaturleater.repository.LinksRepository;
import com.example.wombaturleater.repository.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
@Transactional(readOnly = true)
public class LinksService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789?=()&-/";

    private static final int    BASE     = ALPHABET.length();

    private final PeopleService peopleService;

    private final LinksRepository linksRepository;
//    private final BaseConversion conversion;

    private final PersonDetailsService personDetailsService;

    private final PeopleRepository peopleRepository;

    @Autowired
    public LinksService(PeopleService peopleService, LinksRepository linksRepository, PersonDetailsService personDetailsService, PeopleRepository peopleRepository) {
        this.peopleService = peopleService;
        this.linksRepository = linksRepository;
        this.personDetailsService = personDetailsService;
        this.peopleRepository = peopleRepository;
    }

    public List<Link> findAll() {
        return linksRepository.findAll();
    }

    public List<Link> findAllOwnerLinks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Person> optPerson =  peopleRepository.findByUsername(username);
        if(optPerson.isPresent()){
            Person person = optPerson.get();
            int id = person.getId();
            return linksRepository.findAllLinksByOwner(id);
        } else {
            return Collections.emptyList();
        }
    }




    @Transactional
    public void save(Link link) {
        link.setCreatedDate(new Date());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Person> optPerson =  peopleRepository.findByUsername(username);
        if(optPerson.isPresent()){
            Person person = optPerson.get();
            link.setOwner(person);
        }
        link.setShortLink(cutter(link.getLongLink()));
        linksRepository.save(link);
    }

    public String cutter(String longLinkName){
        StringBuffer sb = new StringBuffer();
        String result = null;
        URL aURL = null;
        try {
            aURL = new URL(longLinkName);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        sb.append(longLinkName);
        String host = cutParts(aURL);
        result = host +"/"+ encode(decode(longLinkName.substring(getProtocol(aURL)+ host.length())));

        return result;

    }

    @Transactional
    public void delete(int id) {
        linksRepository.deleteById(id);
    }

    public Link findOne(int id) {
        Optional<Link> foundUrl = linksRepository.findById(id);
        return foundUrl.orElse(null);
    }


    public static String encode(int num) {
        StringBuilder sb = new StringBuilder();
        while ( num > 0 ) {
            sb.append( ALPHABET.charAt( num % BASE ) );
            num /= BASE;
        }
        return sb.reverse().toString();
    }

    //todo
    public static int decode(String str) {
        int num = 0;
        for ( int i = 0; i < str.length(); i++ )
            num = num * BASE + ALPHABET.indexOf(str.charAt(i));
        return num;
    }

    public static String cutParts(URL url) {
        URL aURL = null;
        aURL = url;
        return aURL.getHost();
    }

    public static int getProtocol(URL url){
        return url.getProtocol().length();
    }

}
