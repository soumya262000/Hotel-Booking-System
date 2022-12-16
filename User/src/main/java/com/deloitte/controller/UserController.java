package com.deloitte.controller;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.project.hb.user.domain.model.entity.Entity;
import com.project.hb.user.domain.model.entity.User;
import com.project.hb.user.domain.service.UserService;
import com.project.hb.user.domain.valueobject.UserVO;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    protected Logger logger = Logger.getLogger(UserController.class.getName());

    protected UserService userService;

  
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @HystrixCommand(fallbackMethod = "defaultUsers")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<User>> findByName(@RequestParam("name") String name) {
        logger.info(String.format("user-service findByName() invoked:%s for %s ", userService.getClass().getName(), name));
        name = name.trim().toLowerCase();
        Collection<User> users;
        try {
            users = userService.findByName(name);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception raised findByName REST Call", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return users.size() > 0 ? new ResponseEntity<>(users, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @HystrixCommand(fallbackMethod = "defaultUser")
    public ResponseEntity<Entity> findById(@PathVariable("id") String id) {
        logger.info(String.format("user-service findById() invoked:%s for %s ", userService.getClass().getName(), id));
        id = id.trim();
        Entity user;
        try {
            user = userService.findById(id);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Exception raised findById REST Call {0}", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return user != null ? new ResponseEntity<>(user, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<User> add(@RequestBody UserVO userVO) {
        logger.info(String.format("user-service add() invoked: %s for %s", userService.getClass().getName(), userVO.getName()));
        System.out.println(userVO);
        User user = new User(null, null, null, null, null);
        BeanUtils.copyProperties(userVO, user);
        try {
            userService.add(user);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Exception raised add User REST Call {0}", ex);
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }


    public ResponseEntity<Entity> defaultUser(String input) {
        logger.warning("Fallback method for user-service is being used.");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<Collection<User>> defaultUsers(String input) {
        logger.warning("Fallback method for user-service is being used.");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
