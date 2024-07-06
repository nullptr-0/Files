const serverAddress = '.';

document.getElementById('upload-form').addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = new FormData();
    formData.append('file', document.getElementById('file').files[0]);
    formData.append('title', document.getElementById('title').value);
    formData.append('description', document.getElementById('description').value);

    const response = await fetch(`${serverAddress}/f/ul`, {
        method: 'POST',
        body: formData
    });

    const result = await response.text();
    document.getElementById('upload-response').textContent = result;
});

document.getElementById('download-button').addEventListener('click', async () => {
    const title = document.getElementById('download-title').value;
    if (title.length === 0) {
        document.getElementById('download-response').textContent = "Title Cannot Left Blank";
        return;
    }

    const response = await fetch(`${serverAddress}/f/dl/${title}`);
    if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = title; // Default filename
        if (contentDisposition) {
            const match = contentDisposition.match(/filename="(.+)"/);
            if (match && match[1]) {
                filename = match[1];
            }
        }
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        document.getElementById('download-response').textContent = "Downloaded File " + filename;
    } else {
        const result = await response.text();
        document.getElementById('download-response').textContent = result;
    }
});

document.getElementById('list-button').addEventListener('click', async () => {
    const response = await fetch(`${serverAddress}/f/ls`);
    const result = await response.json();
    if (result.length === 0) {
        document.getElementById('list-response').textContent = "No Results";
    } else {
        document.getElementById('list-response').textContent = "List Of Files:";
        result.forEach(function (obj) {
            document.getElementById('list-response').textContent += "\n\nTitle: " + obj.title + "\nDescription: " + obj.description + "\nUpload Time: " + obj.uploadTime;
        });
    }
});

document.getElementById('details-button').addEventListener('click', async () => {
    const title = document.getElementById('details-title').value;
    if (title.length === 0) {
        document.getElementById('details-response').textContent = "Title Cannot Left Blank";
        return;
    }

    const response = await fetch(`${serverAddress}/f/dt?title=${title}`);
    if (response.ok) {
        const result = await response.json();
        document.getElementById('details-response').textContent = "File Details:\n\nTitle: " + result.title + "\nDescription: " + result.description + "\nUpload Time: " + result.uploadTime;
    } else if (response.status === 404) {
        document.getElementById('details-response').textContent = "File Not Found";
    }
});

document.getElementById('search-button').addEventListener('click', async () => {
    const title = document.getElementById('search-title').value;
    const date = document.getElementById('search-date').value;
    if (title.length === 0 && date.length === 0) {
        document.getElementById('search-response').textContent = "At Least One Criteria Required";
        return;
    }

    const searchRequest = { title, date };

    const response = await fetch(`${serverAddress}/f/fd`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(searchRequest)
    });

    const result = await response.json();
    if (result.length === 0) {
        document.getElementById('search-response').textContent = "No Results";
    } else {
        document.getElementById('search-response').textContent = "Matched Files:";
        result.forEach(function (obj) {
            document.getElementById('search-response').textContent += "\n\nTitle: " + obj;
        });
    }
});
