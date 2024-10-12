package common.Messages;

public enum MessageType {
    LogIn("Log In de Usuário"),
    CADASTRO_USUARIO("Cadastro de Usuário"),
    ENTRAR_SALA("Entrar na Sala"),
    ENVIAR_CARTELA("Enviar Cartela"),
    SORTEIO("Sorteio"),
    BINGO("Bingo"),
    VENCEDOR("Vencedor"),
    SALAS_DISPONIVEIS("Salas Disponíveis"),
    AVISO_INICIO_SORTEIO("Aviso: O sorteio começou");

    private String descricao;

    MessageType(String descricao) {
        this.descricao = descricao;
    }

    // Método para retornar a descrição
    public String getDescricao() {
        return descricao;
    }
}
