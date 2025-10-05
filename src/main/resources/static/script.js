const API_BASE = window.location.origin;

// Estado global
let currentGasPrice = null;
let ethPrice = 3000; // Valor padrão, será atualizado via API

// Carrega dados ao iniciar
document.addEventListener('DOMContentLoaded', () => {
    loadCurrentGasPrice();
    loadStats();
    loadEthPrice();

    // Atualiza a cada 30 segundos
    setInterval(loadCurrentGasPrice, 30000);
    setInterval(loadStats, 30000);

    // Atualiza preço ETH a cada 5 minutos
    setInterval(loadEthPrice, 300000);

    // Carrega bot username do ambiente (se disponível)
    loadBotUsername();
});

// Formulário de criação de alerta
document.getElementById('alertForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const maxGasPrice = parseInt(document.getElementById('maxGasPrice').value);
    const telegramUsername = document.getElementById('telegramUsername').value.trim();

    const resultDiv = document.getElementById('result');
    resultDiv.className = 'result';
    resultDiv.textContent = 'Criando alerta...';
    resultDiv.classList.remove('hidden');

    try {
        const response = await fetch(`${API_BASE}/api/alert`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                maxGasPrice,
                telegramUsername
            })
        });

        const data = await response.json();

        if (response.ok) {
            resultDiv.className = 'result success';
            resultDiv.textContent = `✓ ${data.message}`;

            // Limpa formulário
            document.getElementById('alertForm').reset();

            // Atualiza estatísticas
            setTimeout(loadStats, 1000);
        } else {
            resultDiv.className = 'result error';
            resultDiv.textContent = `✗ ${data.error || 'Erro ao criar alerta'}`;
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.textContent = '✗ Erro de conexão. Tente novamente.';
        console.error('Erro:', error);
    }
});

// Carrega gas price atual
async function loadCurrentGasPrice() {
    try {
        const response = await fetch(`${API_BASE}/api/gas-price`);

        if (response.ok) {
            const data = await response.json();
            console.log('Gas price response:', data); // Debug

            if (data.gasPrice !== null && data.gasPrice !== undefined) {
                currentGasPrice = data.gasPrice;
                animateValue('currentGas', data.gasPrice, '');
                updateCostCalculator(); // Atualiza calculadora
            } else {
                console.error('Gas price é null na resposta:', data);
                document.getElementById('currentGas').innerHTML =
                    '<span class="loading">Carregando...</span>';

                // Tenta novamente após 5 segundos
                setTimeout(loadCurrentGasPrice, 5000);
            }
        } else {
            const errorData = await response.json();
            console.error('Erro na resposta:', errorData);
            document.getElementById('currentGas').innerHTML =
                '<span class="loading">Indisponível</span>';
        }
    } catch (error) {
        console.error('Erro ao carregar gas price:', error);
        document.getElementById('currentGas').innerHTML =
            '<span class="loading">Erro de conexão</span>';
    }
}

// Carrega estatísticas
async function loadStats() {
    try {
        const response = await fetch(`${API_BASE}/api/stats`);

        if (response.ok) {
            const data = await response.json();

            // Atualiza stats principais
            document.getElementById('activeUsers').textContent = data.activeUsers || 0;
            document.getElementById('totalAlerts').textContent = data.totalAlerts24h || 0;

            // Atualiza navbar
            document.getElementById('navActiveUsers').textContent = data.activeUsers || 0;
            document.getElementById('navTotalAlerts').textContent = data.totalAlerts24h || 0;

            const successRate = data.totalAlerts24h > 0
                ? Math.round((data.successfulAlerts24h / data.totalAlerts24h) * 100)
                : 0;

            document.getElementById('successRate').textContent = `${successRate}%`;
        }
    } catch (error) {
        console.error('Erro ao carregar estatísticas:', error);
    }
}

// Carrega username do bot do Telegram
async function loadBotUsername() {
    // Aqui você pode fazer uma requisição para obter o username do bot
    // Por enquanto, usa uma variável de ambiente ou valor padrão
    const botUsername = 'GasTrackerBot'; // Substitua pelo username real do bot
    document.getElementById('botUsername').textContent = `@${botUsername}`;
}

// Anima valores numéricos
function animateValue(elementId, value, suffix = '') {
    const element = document.getElementById(elementId);
    element.textContent = value + suffix;
}

// Validação em tempo real do username
const usernameInput = document.getElementById('telegramUsername');
usernameInput.addEventListener('input', (e) => {
    let value = e.target.value;

    // Remove espaços
    value = value.replace(/\s/g, '');

    // Adiciona @ se não começar com ele
    if (value && !value.startsWith('@')) {
        value = '@' + value;
    }

    e.target.value = value;
});

// Carrega preço do ETH da CoinGecko
async function loadEthPrice() {
    try {
        const response = await fetch('https://api.coingecko.com/api/v3/simple/price?ids=ethereum&vs_currencies=usd');
        const data = await response.json();

        if (data.ethereum && data.ethereum.usd) {
            ethPrice = data.ethereum.usd;
            document.getElementById('ethPrice').textContent = `$${ethPrice.toLocaleString('en-US', {
                minimumFractionDigits: 0,
                maximumFractionDigits: 0
            })}`;
            updateCostCalculator(); // Atualiza cálculos
        }
    } catch (error) {
        console.error('Erro ao carregar preço do ETH:', error);
        // Mantém valor padrão de $3000
    }
}

// Atualiza calculadora de custos
function updateCostCalculator() {
    if (currentGasPrice === null || currentGasPrice === undefined) {
        return;
    }

    // Operações comuns e seus custos em gas
    const operations = [
        { id: 'transfer', gas: 21000 },
        { id: 'swap', gas: 150000 },
        { id: 'nft', gas: 100000 },
        { id: 'approve', gas: 50000 }
    ];

    operations.forEach(op => {
        // Calcula custo em ETH
        // Fórmula: (gas usado × gas price em Gwei) / 1.000.000.000
        const costInEth = (op.gas * currentGasPrice) / 1_000_000_000;

        // Calcula custo em USD
        const costInUsd = costInEth * ethPrice;

        // Atualiza apenas USD (novo layout)
        const usdElement = document.getElementById(`cost-${op.id}-usd`);
        if (usdElement) {
            usdElement.textContent = `$${costInUsd.toFixed(2)}`;
        }
    });

    // Atualiza status do gas
    updateGasStatus(currentGasPrice);
}

// Atualiza status do gas price
function updateGasStatus(gasPrice) {
    const statusElement = document.getElementById('gasStatus');
    if (!statusElement) return;

    let statusText = '';
    let statusClass = '';

    if (gasPrice <= 5) {
        statusText = '● Excellent time to transact!';
        statusClass = 'status-excellent';
    } else if (gasPrice <= 15) {
        statusText = '● Good gas price';
        statusClass = 'status-good';
    } else if (gasPrice <= 50) {
        statusText = '● Moderate gas price';
        statusClass = 'status-moderate';
    } else {
        statusText = '● High gas price';
        statusClass = 'status-high';
    }

    statusElement.innerHTML = `
        <div class="status-indicator ${statusClass}"></div>
        <span>${statusText}</span>
    `;
}
