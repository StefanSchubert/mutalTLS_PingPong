/*
 * Copyright (c) 2023  by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 *
 */

package de.bluewhale.app.api;


import de.bluewhale.app.services.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/")
public class SampleController {

    @Autowired
    SampleService sampleService;

    @RequestMapping(value = "/certPing", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getLuckyPing() {
        return new ResponseEntity<>(sampleService.clientCertBackendCall(),HttpStatus.OK);
    }

    @RequestMapping(value = "/normalPing", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> getUnhappyPing() {
        return new ResponseEntity<>(sampleService.plainBackendCall(),HttpStatus.OK);
    }




}
