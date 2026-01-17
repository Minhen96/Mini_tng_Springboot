package com.example.mhpractice.features.wallet.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.service.UserService;
import com.example.mhpractice.features.wallet.controller.request.TransferRequest;
import com.example.mhpractice.features.wallet.event.TransferRequestEvent;
import com.example.mhpractice.features.wallet.model.Wallet;
import com.example.mhpractice.features.wallet.service.WalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequest request,
            Authentication authentication) {

        User currentUser = userService.getUserByEmail(authentication.getName());
        Wallet fromWallet = walletService.getWalletByUserId(currentUser.getId());
        Wallet toWallet = walletService.getWalletByUserId(request.getToUserId());

        kafkaTemplate.send("transfer.events.request",
                TransferRequestEvent.of(fromWallet.getId(), toWallet.getId(), request.getAmount()));

        return ResponseEntity.ok(Map.of("message", "Transfer initiated successfully"));
    }

}
