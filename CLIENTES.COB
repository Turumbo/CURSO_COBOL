       IDENTIFICATION      DIVISION.
       PROGRAM-ID. CLIENTES.
       
       ENVIRONMENT         DIVISION.
       CONFIGURATION       SECTION.
       SPECIAL-NAMES.
           DECIMAL-POINT   IS COMMA.
           
       DATA                DIVISION.
       WORKING-STORAGE     SECTION.
       
      *O X INDICA QUE É ALFANUMERICO 
       01  WS-NOME         PIC X(20) VALUE SPACES.
      *O S INDICA QUE É NUMERICO COM SINAL
       01  WS-SALDO        PIC S9(10) COMP VALUE ZEROES.
      *O Z INDICA QUE É NUMERICO E OMITE OS ZEROS A ESQUERDA
       01  WS-SALDO-ED     PIC Z.ZZZ.ZZZ.ZZ9,99.
       
       PROCEDURE DIVISION.
       
      *ATRIBUIÇÃO DE VALORES AS VARIÁVEIS
      
           MOVE "Ricardinhoo"  TO WS-NOME
           MOVE -1300          TO WS-SALDO
           DISPLAY WS-NOME WS-SALDO
           
      *LEITURA DE VALORES PELO TECLADO
           DISPLAY "NOME: "
               ACCEPT WS-NOME.
           DISPLAY "SALDO: "
                ACCEPT WS-SALDO.
           DISPLAY WS-NOME WS-SALDO
           
      *OPERAÇÕES MATEMÁTICAS
      *----SOMAR
      *     ADD 500 TO WS-SALDO
      *        MOVE WS-SALDO TO WS-SALDO-ED
      *     DISPLAY "SALDO ATUAL: " WS-SALDO-ED
      *     DISPLAY "--------------------------------"
           
      *----SUBTRAIR
      *     SUBTRACT 200 FROM WS-SALDO
      *         MOVE WS-SALDO TO WS-SALDO-ED
      *     DISPLAY "SALDO ATUAL: " WS-SALDO-ED
      *     DISPLAY "--------------------------------"
           
      *----MULTIPLICAR           
      *     MULTIPLY WS-SALDO BY 5 GIVING WS-SALDO-ED
      *     DISPLAY "SALDO ATUAL: " WS-SALDO-ED
      *     DISPLAY "--------------------------------"
           
      *----DIVIDIR
      *     DIVIDE WS-SALDO BY 3 GIVING WS-SALDO-ED
      *     DISPLAY "SALDO ATUAL: " WS-SALDO-ED
      *     DISPLAY "--------------------------------"
           
      *----USANDO O COMPUTE
           COMPUTE WS-SALDO = WS-SALDO * 1,10
               MOVE WS-SALDO TO WS-SALDO-ED
           DISPLAY "SALDO ATUAL: " WS-SALDO-ED
           DISPLAY "--------------------------------"
                      
           STOP RUN.
      