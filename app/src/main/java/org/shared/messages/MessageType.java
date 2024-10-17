package org.shared.messages;

public enum MessageType {
    ERRO("Erro"),
    LOG_IN("Log In de Usuário"),
    SUCESSO_LOG_IN("Log In de Usuário com sucesso"),
    CADASTRO_USUARIO("Cadastro de Usuário"),
    ENTRAR_SALA("Entrar na Sala"),
    ENTROU_NA_SALA("Entrar na Sala"),
    ENVIAR_CARTELA("Enviar Cartela"),
    SORTEIO("Sorteio"),
    BINGO("Bingo"),
    VENCEDOR("Vencedor"),
    SALAS_DISPONIVEIS("Salas Disponíveis"),
    AVISO_INICIO_SORTEIO("Aviso: O sorteio começou"),
    PONG("Pong"),
    PING("Ping");

    private String descricao;

    MessageType(String descricao) {
        this.descricao = descricao;
    }

    // Método para retornar a descrição
    public String getDescricao() {
        return descricao;
    }
}
