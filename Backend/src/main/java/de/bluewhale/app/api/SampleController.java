/*
 * Copyright (c) 2023  by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 *
 */

package de.bluewhale.app.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

;

@RestController
@RequestMapping(value = "/")
@Slf4j
public class SampleController {


    @RequestMapping(value = "/pong", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getPongWisdom() {
        log.debug("Retrieved a pong request");
        return new ResponseEntity<>("Hello World",HttpStatus.OK);
    }
}
