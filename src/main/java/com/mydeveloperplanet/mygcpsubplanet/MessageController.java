package com.mydeveloperplanet.mygcpsubplanet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class MessageController {
    @Autowired
    private MyGcpSubPlanetApplication.PubsubOutboundGateway messagingGateway;

    @PostMapping("/postMessage")
    public RedirectView publishMessage(@RequestParam("message") String message) {
        messagingGateway.sendToPubsub(message);
        return new RedirectView("/");
    }
}
