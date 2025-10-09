       IDENTIFICATION      DIVISION.
       PROGRAM-ID. CLIENTES.
       
       ENVIRONMENT         DIVISION.
       DATA                DIVISION.
       WORKING-STORAGE     SECTION.
       01  WS-NOME         PIC X(20) VALUE "Ricardinhoo".
       01  WS-SALDO        PIC 9(06) VALUE 1300.
       
       PROCEDURE DIVISION.
           DISPLAY WS-NOME WS-SALDO.
           DISPLAY "SALDO ATUAL: " WS-SALDO.
           MULTIPLY 300 BY WS-SALDO GIVING WS-SALDO.
           DISPLAY "SALDO ATUAL: " WS-SALDO.
           STOP RUN.
      