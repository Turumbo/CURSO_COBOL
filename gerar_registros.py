import random

# Dados para geração aleatória
nomes = ['Maria', 'Joao', 'Jose', 'Ana', 'Pedro', 'Paulo', 'Carlos', 'Sandra', 'Antonio', 'Teresa']
sobrenomes = ['Silva', 'Santos', 'Oliveira', 'Souza', 'Ferreira', 'Pereira', 'Lima', 'Costa']

with open('EARQCLI.txt', 'wb') as f:
    for _ in range(30):
        # Gerar campos com tamanhos fixos
        agencia = f"{random.randint(1,9999):04}"[:4]  # 4 posições
        conta = f"{random.randint(1,99999):05}"[:5]   # 5 posições
        nome = f"{random.choice(nomes)} {random.choice(sobrenomes)}".ljust(30)[:30]  # 30 posições
        gerente = f"{random.randint(1,999):03}"[:3]   # 3 posições
        tipo_conta = str(random.randint(1,2))         # 1 posição
        saldo = f"{random.randint(0,30000):010d}{random.randint(0,99):02d}"[:12]  # 12 posições
        
        # Criar registro de 55 bytes exatos sem quebra de linha
        registro = f"{agencia}{conta}{nome}{gerente}{tipo_conta}{saldo}".encode('ascii')
        f.write(registro)

print("Arquivo EARQCLI.txt gerado com sucesso!")