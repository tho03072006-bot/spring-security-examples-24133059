'use strict';
(() => {
  const live = document.getElementById('ui-live');
  const announce = text => { if (live) live.textContent = text; };
  document.querySelectorAll('[data-password-toggle]').forEach(button => {
    const input = document.getElementById(button.getAttribute('aria-controls'));
    if (!input) return;
    button.hidden = false;
    button.addEventListener('click', () => {
      const show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      button.textContent = show ? 'Ẩn' : 'Hiện';
      button.setAttribute('aria-pressed', String(show));
      button.setAttribute('aria-label', (show ? 'Ẩn ' : 'Hiện ') + input.dataset.label);
      announce(show ? 'Mật khẩu đang hiển thị.' : 'Mật khẩu đã được ẩn.');
    });
  });
  document.querySelectorAll('.site-nav a').forEach(link => {
    const path = new URL(link.href).pathname;
    const current = location.pathname;
    if ((path === '/' && (current === '/' || current === '/dashboard')) ||
        (path !== '/' && (current === path || current.startsWith(path + '/')))) {
      link.setAttribute('aria-current', 'page');
    }
  });
  const summary = document.querySelector('.error-summary');
  if (summary) summary.focus();
  document.querySelectorAll('[data-character-count]').forEach(input => {
    const output = document.getElementById(input.dataset.characterCount);
    const update = () => { if (output) output.textContent = `${input.value.length.toLocaleString('vi-VN')} / ${input.maxLength.toLocaleString('vi-VN')} ký tự`; };
    input.addEventListener('input', update); update();
  });
  const file = document.getElementById('image');
  if (file && file.type === 'file') {
    const preview = document.getElementById('image-preview');
    const picture = document.getElementById('preview-image');
    const name = document.getElementById('file-name');
    const error = document.getElementById('image-error');
    const clear = document.getElementById('clear-image');
    const original = picture.getAttribute('src') || '';
    let objectUrl;
    const reset = () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl);
      objectUrl = null; file.setCustomValidity(''); file.removeAttribute('aria-invalid');
      error.textContent = '';
      if (original) picture.src = original; else picture.removeAttribute('src');
      preview.hidden = !original;
      name.textContent = original ? 'Ảnh hiện tại' : ''; clear.hidden = true;
    };
    file.addEventListener('change', () => {
      reset();
      const selected = file.files[0];
      if (!selected) return;
      const allowed = /\.(png|jpe?g|gif|bmp)$/i.test(selected.name);
      const message = !allowed ? 'Chọn ảnh PNG, JPG, GIF hoặc BMP.' : selected.size > 10 * 1024 * 1024 ? 'Ảnh vượt quá 10 MB. Vui lòng chọn ảnh nhỏ hơn.' : '';
      if (message) { error.textContent = message; file.setCustomValidity(message); file.setAttribute('aria-invalid', 'true'); announce(message); return; }
      objectUrl = URL.createObjectURL(selected); picture.src = objectUrl;
      preview.hidden = false; clear.hidden = false; name.textContent = selected.name;
      announce('Đã chọn ảnh ' + selected.name);
    });
    clear.addEventListener('click', () => { file.value = ''; reset(); file.focus(); announce('Đã bỏ ảnh vừa chọn.'); });
    reset();
  }
  const dialog = document.getElementById('delete-dialog');
  let pendingForm, returnFocus;
  window.iotstarConfirm = (form, event) => {
    if (form.dataset.confirmed === 'true') return true;
    if (!dialog || typeof dialog.showModal !== 'function') return confirm(form.dataset.confirm);
    event.preventDefault(); pendingForm = form; returnFocus = document.activeElement;
    document.getElementById('delete-description').textContent = form.dataset.confirm;
    dialog.showModal(); document.getElementById('delete-cancel').focus(); return false;
  };
  document.getElementById('delete-confirm')?.addEventListener('click', () => {
    const form = pendingForm; dialog.close();
    if (form) { form.dataset.confirmed = 'true'; form.requestSubmit(); }
  });
  dialog?.addEventListener('close', () => { returnFocus?.focus(); pendingForm = null; });
  document.querySelectorAll('form[method="post"]').forEach(form => {
    form.addEventListener('submit', event => {
      if (event.defaultPrevented) return;
      if (form.dataset.submitting === 'true') { event.preventDefault(); return; }
      form.dataset.submitting = 'true'; form.setAttribute('aria-busy', 'true');
      const button = event.submitter || form.querySelector('button[type="submit"]');
      if (button) {
        button.dataset.originalHtml = button.innerHTML;
        button.textContent = button.dataset.loadingText || 'Đang xử lý…';
        // Disable after form data is constructed, preserving submitted button values.
        setTimeout(() => { button.disabled = true; }, 0);
      }
    });
  });
  window.addEventListener('pageshow', () => {
    document.querySelectorAll('form[data-submitting]').forEach(form => {
      delete form.dataset.submitting; delete form.dataset.confirmed; form.removeAttribute('aria-busy');
      form.querySelectorAll('button[data-original-html]').forEach(button => {
        button.innerHTML = button.dataset.originalHtml; button.disabled = false; delete button.dataset.originalHtml;
      });
    });
  });
})();
