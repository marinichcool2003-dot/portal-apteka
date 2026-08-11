package com.apteka.portal.dtos.response.client;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.Client;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Пользователь со всеми правами и действиями")
public record ClientResponseWithActionsDTO(
        @Schema(description = "Все данные пользоваеля без действий")
        ClientResponseDTO clientResponseDTO,
        @Schema(description = "Все действия пользователя")
        Set<AccountAction> actions
) {
    public static ClientResponseWithActionsDTO from(Client client) {
        return new ClientResponseWithActionsDTO(
                ClientResponseDTO.from(client),
                client.getAccount().getActions()
        );
    }
}
