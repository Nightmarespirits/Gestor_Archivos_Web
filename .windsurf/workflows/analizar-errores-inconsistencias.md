---
description: analizar errores y posibles inconsistencias en un modulo
---

1. primero buscar y revisar el controlador especifico para el modulo del backend (omitir si ya se hizo), tener en claro de que se trata y cuales son los endpoints y parametros, etc que el controlador ofrece.
2. Luego revisar el store en el frontend en la carpeta store buscar y revisar el store para el modulo especifico, revisar que sea consistente con el controller, recordar que las peticiones se centralizan en el service api.js.
3.Despues de tener en claro como esta funcionando el store, revisar en pages todo el codigo de la carpeta uno por uno para ver si esta siendo consistende con el store y esta enviando correctaente los datos ( aqui debemos comparar que el formulario que envia los datos sea consistente con los DTOs de el modulo que se esta analizando) para evitar cualquier error.
4.Finalizar dando un pequeño informe y una propuesta de solucion.