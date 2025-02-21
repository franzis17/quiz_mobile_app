const PORT = 8000;
const CHUNK_SIZE = 10;     // Send X bytes at a time
const CHUNK_DELAY = 20;    // Wait Y ms between sending chunks
const IGNORE_URL = true;   // If true, return data regardless of whether the URL is correct.
const CORRUPTION_PROB = 0; // Probability (0-1) that the returned content will be corrupted.

const REQUIRED_PATH = "/random/question/";
const REQUIRED_PARAMS = {
    'method': 'thedata.getit',
    'api_key': '01189998819991197253',
    'format': 'json'
};

// The server will send the following, converted into JSON. (FYI, since this is Javascript, the 
// JSON will look the same as the original JavaScript code.)
// content = getContent();



// --- Server Logic ---

const https = require('https');
const fs = require('fs');
const os = require('os');
const url = require('url');

const options = {
    key: fs.readFileSync('key.pem'),
    cert: fs.readFileSync('cert.crt')
};

function requestHandler(request, response)
{
    console.log(`Request from ${request.socket.remoteAddress} -- ${request.method} ${request.url}`);
    
    const reqUrl = new url.URL(`https://localhost${request.url}`);
    console.log(`  * path = ${reqUrl.pathname}, parameters = ${reqUrl.searchParams}`);
    
    // Ensure the request is correct.
    let errors = [];
    if(IGNORE_URL)
    {
        console.log("  * ignoring path and parameters (IGNORE_URL == true)")
    }
    else
    {
        if(reqUrl.pathname === REQUIRED_PATH)
        {
            console.log("  * correct path");
        }
        else
        {
            errors.push("wrong path"); 
            console.log("  * wrong path -- rejecting request!");
        }
        
        for(let p in REQUIRED_PARAMS)
        {
            if(!reqUrl.searchParams.has(p))
            {
                errors.push(`missing '${p}' value`);
                console.log(`  * missing '${p}' value -- rejecting request!`);
            }
            else if(reqUrl.searchParams.get(p) !== REQUIRED_PARAMS[p])
            {
                errors.push(`wrong '${p}' value`);
                console.log(`  * incorrect '${p}' value -- rejecting request!`);
            }
            else
            {
                console.log(`  * correct '${p}' value`);
            }
        }
    }
        
    if(errors.length === 0)
    {        
        // No errors found in the request, so return the data.
        let textContent = getContent();
        
        if(Math.random() < CORRUPTION_PROB)
        {
            console.log('  * result corrupted');
            
            // Corrupt the return result by picking and replacing a bracket, brace or quote.            
            const candidateIndices = []
            for(let i = 0; i < textContent.length; i++)
            {
                if(/[\[\]{}"]/.test(textContent.charAt(i)))
                {
                    candidateIndices.push(i);
                }
            }
            
            const index = candidateIndices[Math.floor(Math.random() * candidateIndices.length)];
            textContent = textContent.substring(0, index) + "@@@@@" + 
                          textContent.substring(index + 1);
        }
            
        response.writeHead(200, {
            'Content-Length': textContent.length,
            'Content-Type': 'text/plain'
        });
        
        respondIncrementally(textContent, response);
    }
    else
    {
        // Something was wrong in the request, so behave as if we couldn't find the data.
        response.writeHead(200, {
            'Content-Type': 'text/plain'
        });
        response.end("404 - not found\n\n" + errors.join('\n'));
    }
    
    console.log("");
}

function respondIncrementally(partialContent, response)
{
    if(partialContent.length <= CHUNK_SIZE)
    {
        response.end(partialContent);
    }
    else
    {
        response.write(partialContent.substring(0, CHUNK_SIZE));
        setTimeout(respondIncrementally, CHUNK_DELAY, 
                   partialContent.substring(CHUNK_SIZE), response);
    }
}

const server = https.createServer(options, requestHandler);

server.listen(PORT, (err) => {
    if(err) 
    {
        console.log('Server failed to start: ', err);
    }
    else
    {
        // Find the machine's externally-visible IP address(es), for display purposes only.
        const addresses = []
        const interfaces = os.networkInterfaces();
        for(let intf in interfaces)
        {
            const addressList = interfaces[intf]
            for(let i = 0; i < addressList.length; i++)
            {
                if(!addressList[i].internal)
                {
                    addresses.push(addressList[i].address);
                }
            }
        }
                
        console.log(`HTTPS server, listening on port ${PORT}, at IPs ${addresses.join(", ")} (plus loopback)`);
        console.log(`Artificial slow-down: sending ${CHUNK_SIZE} byte(s) at a time, with ${CHUNK_DELAY}ms delay between them`);
        console.log(`Corruption probability = ${CORRUPTION_PROB}`);
    }
});

function getContent(){
    var first_number= getRandomInt(1000);
    var second_number= getRandomInt(1000);
    var option = getRandomInt(3);
    var resultValue = 0;
    var questionString;
    if(option ==0){
        questionString = "Solve the following: "+first_number+" + "+ second_number+" = ?";
        resultValue = first_number + second_number;  
    }
    else if(option ==1){
        questionString = "Solve the following: "+first_number+" - "+ second_number+" = ?";
        resultValue = first_number - second_number;  
    }
    else {
        first_number = getRandomInt(100);
        second_number = getRandomInt(100);
        questionString = "Solve the following: "+first_number+" * "+ second_number+" = ?"; 
        resultValue = first_number * second_number; 
    }

    var max = resultValue + 1000;
    var min = resultValue - 1000;

    var arrayOptions = [];

    var firstFilter = getRandomInt(6);

    if (firstFilter > 1) {

        arrayOptions.push(resultValue);

        var numberOfOptions = randomNumberIntRange(1, 10); // Was 15

        var i = 1;
        for (i = 1; i <= numberOfOptions; i++) {
            arrayOptions.push(randomNumberIntRange(min, max));
        }
    }

    var defaultTime = 60;

    if (arrayOptions.length < 1) {
        defaultTime = defaultTime + 60;
    }
    else {
        defaultTime = defaultTime + 10 * arrayOptions.length;
    }


    return JSON.stringify({ question: questionString, result: resultValue, options: shuffle(arrayOptions), timetosolve: defaultTime });
}

function getRandomInt(max) {
    return Math.floor(Math.random() * max);
}

function randomNumberIntRange(min, max) {
    const r = Math.random() * (max - min) + min
    return Math.floor(r)
}

function shuffle(array) {
    return array.sort(() => Math.random() - 0.5);
}
