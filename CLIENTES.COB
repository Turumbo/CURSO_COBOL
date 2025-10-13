       IDENTIFICATION      DIVISION.
       PROGRAM-ID. CLIENTES.
       
       ENVIRONMENT         DIVISION.
       CONFIGURATION       SECTION.
       SPECIAL-NAMES.
           DECIMAL-POINT   IS COMMA.
       INPUT-OUTPUT        SECTION.
       FILE-CONTROL.
           SELECT ARQ ASSIGN TO "./EARQCLI.txt"
               FILE STATUS IS FS-ARQ.
       DATA                DIVISION.
       FILE                SECTION.
       FD  ARQ
           RECORDING MODE IS FIXED.
       01  REGISTRO.
           05 REG-AGENCIA      PIC X(04).
           05 REG-CONTA        PIC X(05).
           05 REG-NOME         PIC X(30).
           05 REG-GERENTE      PIC X(03).
           05 REG-TIPO-CONTA   PIC 9.
           05 REG-SALDO        PIC 9(10)V99.
      *LAYOUT 55 POSICOES
       
      *O X INDICA QUE É ALFANUMERICO 
      * 01  WS-NOME         PIC X(20) VALUE SPACES.
      *O S INDICA QUE É NUMERICO COM SINAL
      * 01  WS-SALDO        PIC S9(10) COMP VALUE ZEROES.
      *O Z INDICA QUE É NUMERICO E OMITE OS ZEROS A ESQUERDA
      * 01  WS-SALDO-ED     PIC Z.ZZZ.ZZZ.ZZ9,99.
      * 01  WS-TIPO-CONTA   PIC 9.
       
       WORKING-STORAGE     SECTION.
       
      *----------VARIAVEIS DE STATUS-----------------
       01  FS-ARQ              PIC 9(02).
      *----------VARIAVEIS ACUMULADORAS--------------
       01  WS-CONTADOR         PIC 9(03) USAGE COMP.
       01  WS-CONTADOR-VALIDO  PIC 9(03) USAGE COMP.
       01  WS-CONTADOR-POBRE   PIC 9(03) USAGE COMP.

       PROCEDURE DIVISION.
       
       0100-PRINCIPAL      SECTION.
       
      *     PERFORM VARYING WS-CONTADOR FROM 0 BY 1 
      *             UNTIL WS-TIPO-CONTA EQUAL 9
      *         PERFORM 0200-INICIAR
      *         PERFORM 0300-PROCESSAR
      *     END-PERFORM.
           PERFORM 0200-INICIAR.
           PERFORM 0300-PROCESSAR UNTIL FS-ARQ NOT EQUAL ZEROS.
           PERFORM 0400-FINALIZAR.
           STOP RUN.
       0100-FIM.           EXIT.
       
       0200-INICIAR        SECTION.
      *LEITURA DE VALORES PELO TECLADO
      *     DISPLAY "NOME: "
      *         ACCEPT REG-NOME.
      *     DISPLAY "SALDO: "
      *         ACCEPT REG-SALDO.
      *     DISPLAY "TIPO DE CONTA (1-PF 2-PJ 9-SAIR): "
      *         ACCEPT REG-TIPO-CONTA.
      *     DISPLAY "--------------------------------".
           OPEN INPUT ARQ.
           IF FS-ARQ NOT EQUAL ZEROS
               DISPLAY "ERRO NA ABERTURA DO ARQUIVO"
               GOBACK
           END-IF.
           
           PERFORM 0210-LEITURA.
      
       0200-FIM.           EXIT.
       
       0210-LEITURA        SECTION.
           READ ARQ.
       0210-FIM.           EXIT.
       
       0300-PROCESSAR      SECTION.
           ADD 1 TO WS-CONTADOR
           
           IF REG-SALDO GREATER THAN OR EQUAL 5000
               AND (REG-TIPO-CONTA = 1 OR REG-TIPO-CONTA = 2)
      *         MOVE REG-SALDO TO REG-SALDO-ED
               DISPLAY REGISTRO
               ADD 1 TO WS-CONTADOR-VALIDO
           ELSE
               EVALUATE TRUE
                   WHEN REG-SALDO < 5000
                       DISPLAY "POBRE"
                   WHEN REG-TIPO-CONTA NOT = 1 
                        AND REG-TIPO-CONTA NOT = 2
                       DISPLAY "TIPO DE CONTA INVALIDO"
               END-EVALUATE
               ADD 1 TO WS-CONTADOR-POBRE
           END-IF.
           
           PERFORM 0210-LEITURA.
       0300-FIM.           EXIT.
       
       0400-FINALIZAR       SECTION.
           CLOSE ARQ
           DISPLAY "--------------------------------".
           DISPLAY "-----------ESTATISTICA----------".
           DISPLAY "CLIENTES LIDOS....." WS-CONTADOR.
           DISPLAY "CLIENTES VALIDOS..." WS-CONTADOR-VALIDO.
           DISPLAY "CLIENTES POBRES...." WS-CONTADOR-POBRE.
       0400-FIM.            EXIT.
       
      *ATRIBUIÇÃO DE VALORES AS VARIÁVEIS
      
      *     MOVE "Ricardinhoo"  TO WS-NOME
      *     MOVE -1300          TO WS-SALDO
      *     DISPLAY WS-NOME WS-SALDO
           
      

           
           
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
      *     COMPUTE WS-SALDO = WS-SALDO * 1,10
      *         MOVE WS-SALDO TO WS-SALDO-ED
      *     DISPLAY "SALDO ATUAL: " WS-SALDO-ED
      *     DISPLAY "--------------------------------"
           