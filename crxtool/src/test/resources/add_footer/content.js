// run_at: document_end means DOM has been created
const div = document.createElement("div");
div.id = "injected-content";
div.innerText = "hello, world";
document.body.appendChild(div);