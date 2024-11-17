const API_URL = 'http://localhost:7000/api';

function showMessage(text, isError = false) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = text;
    messageDiv.className = isError ? 'error' : 'success';
    setTimeout(() => messageDiv.textContent = '', 3000);
}

function loadStudents() {
    $.ajax({
        url: `${API_URL}/students`,
        method: 'GET',
        success: function(students) {
            const tbody = document.getElementById('studentsList');
            tbody.innerHTML = '';

            students.forEach(student => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${student.id}</td>
                    <td>${student.firstName}</td>
                    <td>${student.lastName}</td>
                    <td>${student.middleName || ''}</td>
                    <td>${formatDate(student.birthDate)}</td>
                    <td>${student.group}</td>
                    <td>
                        <button onclick="deleteStudent(${student.id})">Удалить</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        },
        error: function(xhr) {
            showMessage('Ошибка при загрузке списка студентов', true);
        }
    });
}

function formatDate(dateStr) {
    const [year, month, day] = dateStr.split('-');
    return `${day}.${month}.${year}`;
}

function deleteStudent(id) {
    if (!confirm('Вы уверены, что хотите удалить этого студента?')) {
        return;
    }

    $.ajax({
        url: `${API_URL}/students/${id}`,
        method: 'DELETE',
        success: function() {
            showMessage('Студент успешно удален');
            loadStudents();
        },
        error: function(xhr) {
            showMessage('Ошибка при удалении студента', true);
        }
    });
}

document.getElementById('addStudentForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const student = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        middleName: document.getElementById('middleName').value || null,
        birthDate: document.getElementById('birthDate').value,
        group: document.getElementById('group').value
    };

    $.ajax({
        url: `${API_URL}/students`,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(student),
        success: function(response) {
            showMessage('Студент успешно добавлен');
            this.reset();
            loadStudents();
        }.bind(this),
        error: function(xhr) {
            let message = 'Ошибка при добавлении студента';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                message = xhr.responseJSON.message;
            }
            showMessage(message, true);
        }
    });
});

document.addEventListener('DOMContentLoaded', loadStudents);