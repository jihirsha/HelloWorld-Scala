package hello;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
//import com.fasterxml.jackson.annotation.JacksonAnnotation;

@RestController
//@RequestMapping("api/v1")
public class ModeratorController {

    AtomicInteger count = new AtomicInteger();
    AtomicInteger count_poll = new AtomicInteger(123456789);
    static final ConcurrentHashMap<Integer,Moderator> moderator = new ConcurrentHashMap<Integer, Moderator>();
    public static final ConcurrentHashMap<Integer, ArrayList<Poll>> m_poll = new ConcurrentHashMap<Integer, ArrayList<Poll>>();
    static final ConcurrentHashMap<String, Poll> poll_detail = new ConcurrentHashMap<String, Poll>();

  /*  @RequestMapping(name = "/hello" ,method = RequestMethod.POST)
    public Moderator temp()
    {
        Moderator m = new Moderator();
        m.setName("abc");
        m.setPassword("abc");
        m.setEmail("abc");
        m.setid(count.incrementAndGet());
        m.setCreated_at();
        moderator.putIfAbsent(m.getid(),m);
        return m;
    }*/

    //@EnableGlobalAuthentication(value = "Authorization")
    //1. creating moderator
    @RequestMapping(name = "api/v1/moderators", method = RequestMethod.POST,
            consumes = "application/json",produces ="application/json")
    public ResponseEntity method0( @Valid @RequestBody Moderator mod, BindingResult result)//,@RequestHeader (value = "Authorization") String str) {

    {
        //if (BasicAuth.checkURL(str)) {

            List errorlist = new ArrayList();
            Moderator mod_new = new Moderator();

            if(mod.getName() != "" || mod.getName()!=null) {

                mod_new.setName(mod.getName());
                mod_new.setEmail(mod.getEmail());
                mod_new.setPassword(mod.getPassword());
                mod_new.id = count.incrementAndGet();
                mod_new.setCreated_at();
                moderator.putIfAbsent(mod_new.getid(), mod_new);
            }
             if(mod.getName()== "" || mod.getName()== null) {
             String strName = "Name field can not be empty/null";
             errorlist.add(strName);
         }

            if (result.hasErrors() || mod.getName()== "" || mod.getName()==null) {

                List<FieldError> fieldErrors = result.getFieldErrors();
                String error;
                for (FieldError fielderror : fieldErrors) {
                    error = fielderror.getField() + "  " + fielderror.getDefaultMessage();
                    errorlist.add(error);
                }
                return new ResponseEntity(errorlist, HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<Moderator>(mod_new, HttpStatus.CREATED);
            }
        //}
        //else
            //return new ResponseEntity("Not Authorized", HttpStatus.BAD_REQUEST);

    }


    //2. view moderator resource
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "api/v1/moderators/{id}",method = RequestMethod.GET)
    public ResponseEntity<Moderator> findModerator(@PathVariable("id") int id,@RequestHeader("Authorization")String str) {
        if(BasicAuth.checkURL(str)) {
            Moderator mod = moderator.get(id);
            HttpHeaders header = new HttpHeaders();
            header.add("Accept","Application/json");
            return new ResponseEntity<Moderator>(mod,header,HttpStatus.OK);
        }
        else
            return new ResponseEntity("",HttpStatus.UNAUTHORIZED);
    }

    //3. update moderator
    @RequestMapping(value = "api/v1/moderators/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Moderator> updateModerator(@PathVariable("id") int id, @Valid Moderator m, BindingResult result,@RequestHeader("Authorization")String str) {
        if (BasicAuth.checkURL(str)) {
            Moderator mod = moderator.get(id);
            mod.setEmail(m.getEmail());
            mod.setPassword(m.getPassword());

            if (result.hasErrors()) {
                List errorlist = new ArrayList();
                List<FieldError> fieldErrors = result.getFieldErrors();
                String error;
                for (FieldError fielderror : fieldErrors) {
                /*if(fielderror.getField().compareTo("name")==0)
                {
                    continue;
                }*/

                    error = fielderror.getField() + "  " + fielderror.getDefaultMessage();
                    errorlist.add(error);

                }
                return new ResponseEntity(errorlist, HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<Moderator>(mod, HttpStatus.OK);
            }
        }
        else
            return new ResponseEntity("",HttpStatus.UNAUTHORIZED);
    }

    //4. create a poll
    @JsonView(View.results.class)
    @RequestMapping(value = "api/v1/moderators/{id}/polls", method = RequestMethod.POST)
    public ResponseEntity<Poll> addNewPoll(@PathVariable("id") int id, @Valid Poll poll, BindingResult result ,@RequestHeader("Authorization")String str) {
        if(BasicAuth.checkURL(str)){
        Poll p = new Poll();
        p.setQuestion(poll.getQuestion());
        p.setChoice(poll.getChoice());
        p.setStarted_at(poll.getstarted_at());
        p.setExpired_at(poll.getExpired_at());

        p.id = Integer.toString(count_poll.incrementAndGet(), 36);

        if (!m_poll.containsKey(id)) {
            ArrayList<Poll> polls = new ArrayList<Poll>();
            polls.add(p);
            m_poll.putIfAbsent(id, polls);
        } else {
            ArrayList<Poll> polls = m_poll.get(id);
            polls.add(p);
        }
        poll_detail.putIfAbsent(p.getId(), p);
        if (result.hasErrors()) {
            List errorlist = new ArrayList();
            List<FieldError> fieldErrors = result.getFieldErrors();
            String error;
            for (FieldError fielderror : fieldErrors) {
                error = fielderror.getField() + " " + fielderror.getDefaultMessage();
                errorlist.add(error);
            }

            return new ResponseEntity(errorlist, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<Poll>(p, HttpStatus.CREATED);
        }
    }
        else
            return new ResponseEntity("",HttpStatus.UNAUTHORIZED);
    }

    //5. View a poll without result
    @JsonView(View.results.class)
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "api/v1/polls/{id}", method = RequestMethod.GET)
    public Poll viewPoll(@PathVariable("id") String id) {

        Poll p = poll_detail.get(id);
        return p;
    }

    //6. View a poll with result

    @JsonView(View.viewwithresults.class)
    @RequestMapping(value = "api/v1/moderators/{moderator_id}/polls/{id}", method = RequestMethod.GET)
    public ResponseEntity viewPollWithResults(@PathVariable("moderator_id") int moderator_id, @PathVariable("id") String id,@RequestHeader("Authorization")String str) {
        if(BasicAuth.checkURL(str)){
        Poll p = poll_detail.get(id);
        return new ResponseEntity(p,HttpStatus.OK);
    }
        else
        return new ResponseEntity("",HttpStatus.UNAUTHORIZED);
    }


    //7.List all polls
    @RequestMapping(value = "api/v1/moderators/{id}/polls", method = RequestMethod.GET)
    public ResponseEntity viewPoll(@PathVariable("id") int id,@RequestHeader("Authorization")String str) {
        if (BasicAuth.checkURL(str)) {

            ArrayList<Poll> all_Polls = new ArrayList<Poll>();
            Set<Integer> keyset = m_poll.keySet();
            Iterator it = keyset.iterator();
            while (it.hasNext()) {
                int key = Integer.parseInt(it.next().toString());
                if (key == id) {
                    //System.out.println(key);
                    all_Polls = m_poll.get(id);
                    break;
                }
            }
            return new ResponseEntity(all_Polls,HttpStatus.OK);
        }
        else
            return new ResponseEntity("",HttpStatus.UNAUTHORIZED);
    }

    //8.Delete a poll
    @RequestMapping(value = "api/v1/moderators/{moderator_id}/polls/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity deletePoll(@PathVariable("id") String id, @PathVariable("moderator_id") int mid,@RequestHeader("Authorization")String str) {
        /* removing from the list of moderator and polls */
        if (BasicAuth.checkURL(str)) {
            Boolean deleted = false;
            ArrayList<Poll> polls = new ArrayList<Poll>();
            Set<Integer> keyset = m_poll.keySet();
            Iterator it = keyset.iterator();
            Poll pollDelete = poll_detail.get(id);
            while (it.hasNext()) {

                int key = Integer.parseInt(it.next().toString());
                if (key == mid) {
                    polls = m_poll.get(mid);
                    deleted = polls.remove(pollDelete);

                }
            }
        /* removing from the list of poll */
            Set<String> keyset_poll = poll_detail.keySet();
            Iterator it_poll = keyset.iterator();
            while (it_poll.hasNext()) {

                String key = it_poll.next().toString();
                if (key.compareTo(id) == 0) {
                    poll_detail.remove(key);
                }
            }
            return new ResponseEntity("",HttpStatus.NO_CONTENT);
        } else
            return new ResponseEntity("",HttpStatus.UNAUTHORIZED);
    }
    //9.Vote a poll

    @RequestMapping(value = "api/v1/polls/{id}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity doVote(@RequestParam ("choice") int choice, @PathVariable("id") String id) {
        Poll p = new Poll();
        p = poll_detail.get(id);
        if (p != null) {
            ArrayList<Integer> results = p.getResults();
            int toIncrement = results.get(choice);
            results.set(choice, ++toIncrement);
            p.setresults(results);

        /*if (result.hasErrors()) {
            List errorlist = new ArrayList();
            List<FieldError> fieldErrors = result.getFieldErrors();
            String error;
            for (FieldError fielderror : fieldErrors) {
                error = fielderror.getField() + " " + fielderror.getDefaultMessage();
                errorlist.add(error);
            }*/
            return new ResponseEntity("", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity("Not valid Poll", HttpStatus.BAD_REQUEST);

    }
}