import logo from '../assets/static-images/logo.png'

import '../styles/login/Login.css'

export default function Login() {
    return (
        <div className='login-body'>
            <div className='login__container'>
                <header className='login-header'>
                    <img src={logo} alt="Логотип Социальных аптек" />
                    <h1>Социальная аптека</h1>
                    <span>Вход в систему</span>
                </header>
                <form className='login-form' action="submit">
                    <div className='username-input__container'>
                        <label htmlFor="">Логин</label>
                        <input type="text" placeholder='Введите логин' />
                    </div>
                    <div className='password-input__container'>
                        <label htmlFor="">Пароль</label>
                        <input type="password" placeholder='Введите пароль' />
                    </div>
                    <div className='remember-me__container'>
                        <label htmlFor="">Запомнить меня</label>
                        <input type="checkbox" name="" id="" />
                    </div>
                    <button type="submit">Войти</button>
                </form>
            </div>
        </div>
    )
}