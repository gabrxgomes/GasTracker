package com.gastracker.service;

import com.gastracker.model.User;
import com.gastracker.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Optional;

@Service
@Slf4j
public class TelegramBotService implements LongPollingSingleThreadUpdateConsumer {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final UserRepository userRepository;
    private TelegramClient telegramClient;
    private TelegramBotsLongPollingApplication botsApplication;
    private BotSession botSession;

    public TelegramBotService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        try {
            // Inicializa cliente
            telegramClient = new OkHttpTelegramClient(botToken);

            // Inicializa long polling application
            botsApplication = new TelegramBotsLongPollingApplication();

            // Registra o bot para receber atualizações
            botSession = botsApplication.registerBot(botToken, this);

            log.info("Telegram Bot inicializado e registrado com sucesso: {}", botUsername);
        } catch (Exception e) {
            log.error("Erro ao inicializar Telegram Bot: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (botSession != null) {
                botSession.stop();
            }
            if (botsApplication != null) {
                botsApplication.close();
            }
            log.info("Telegram Bot desligado com sucesso");
        } catch (Exception e) {
            log.error("Erro ao desligar Telegram Bot: {}", e.getMessage(), e);
        }
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();

            if ("/start".equals(messageText)) {
                handleStartCommand(chatId, username);
            } else if ("/status".equals(messageText)) {
                handleStatusCommand(chatId);
            } else if ("/stop".equals(messageText)) {
                handleStopCommand(chatId);
            } else {
                sendMessage(chatId, "Comando não reconhecido. Use /start para começar.");
            }
        }
    }

    private void handleStartCommand(Long chatId, String username) {
        if (username == null || username.isBlank()) {
            sendMessage(chatId, "[ERROR] You need to configure a Telegram username to use this bot.");
            return;
        }

        // Normaliza username para lowercase (igual ao ValidationService)
        String normalizedUsername = username.toLowerCase();

        Optional<User> existingUser = userRepository.findByTelegramUsername(normalizedUsername);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setChatId(chatId);
            user.setIsActive(true);
            userRepository.save(user);

            sendMessage(chatId, String.format(
                "[ACTIVE] Welcome back, @%s!\n\n" +
                "Your alert is active for gas price ≤ %d Gwei.\n\n" +
                "Available commands:\n" +
                "/status - View current settings\n" +
                "/stop - Disable alerts",
                username, user.getMaxGasPrice()
            ));
        } else {
            sendMessage(chatId, String.format(
                "[INFO] Hello, @%s!\n\n" +
                "To create an alert, visit our landing page and configure your gas price threshold.\n\n" +
                "After setup, use /start again to activate notifications.",
                normalizedUsername
            ));
        }
    }

    private void handleStatusCommand(Long chatId) {
        Optional<User> userOpt = userRepository.findByChatId(chatId);

        if (userOpt.isEmpty()) {
            sendMessage(chatId, "[ERROR] You are not registered yet. Configure your alert on the landing page.");
            return;
        }

        User user = userOpt.get();
        String status = user.getIsActive() ? "[ACTIVE]" : "[INACTIVE]";

        sendMessage(chatId, String.format(
            "ALERT STATUS\n\n" +
            "Username: @%s\n" +
            "Max Gas Price: %d Gwei\n" +
            "Status: %s\n" +
            "Last notification: %s",
            user.getTelegramUsername(),
            user.getMaxGasPrice(),
            status,
            user.getLastNotificationAt() != null ? user.getLastNotificationAt().toString() : "Never"
        ));
    }

    private void handleStopCommand(Long chatId) {
        Optional<User> userOpt = userRepository.findByChatId(chatId);

        if (userOpt.isEmpty()) {
            sendMessage(chatId, "[ERROR] You are not registered.");
            return;
        }

        User user = userOpt.get();
        user.setIsActive(false);
        userRepository.save(user);

        sendMessage(chatId, "[SUCCESS] Alerts disabled. Use /start to reactivate.");
    }

    /**
     * Envia alerta de gas price para um usuário
     */
    public boolean sendGasAlert(Long chatId, Integer currentGasPrice, Integer userThreshold) {
        String message = String.format(
            "[GAS ALERT]\n\n" +
            "Current gas: %d Gwei\n" +
            "Your limit: %d Gwei\n\n" +
            "» Good time to make transactions!",
            currentGasPrice, userThreshold
        );

        return sendMessage(chatId, message);
    }

    /**
     * Envia mensagem para um chat
     */
    public boolean sendMessage(Long chatId, String text) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build();
            telegramClient.execute(message);
            return true;
        } catch (TelegramApiException e) {
            log.error("Erro ao enviar mensagem Telegram para chatId {}: {}", chatId, e.getMessage());
            return false;
        }
    }
}
