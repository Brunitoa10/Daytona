/*
 * Daytona - lectura de joysticks, volantes, pedales, palancas (WinMM / DirectInput)
 * y gamepads Xbox (XInput) para Java 8+ vía JNI.
 * Compatible con Windows 7, 8, 10 y 11 (x86 y x64). Sin dependencias extra:
 * winmm.dll es parte de Windows y XInput se carga dinámicamente si existe.
 *
 * Compilar (MinGW-w64):
 *   x86_64-w64-mingw32-gcc -O2 -shared -Iinclude daytona_input.c -o daytona_input_x64.dll -lwinmm -static-libgcc
 *   i686-w64-mingw32-gcc   -O2 -shared -Iinclude daytona_input.c -o daytona_input_x86.dll -lwinmm -static-libgcc -Wl,--kill-at
 */
#include <windows.h>
#include <mmsystem.h>
#include <jni.h>

/* --- XInput cargado dinámicamente (xinput1_4 en Win8+, 1_3 / 9_1_0 en Win7) --- */
typedef struct {
    WORD wButtons;
    BYTE bLeftTrigger;
    BYTE bRightTrigger;
    SHORT sThumbLX;
    SHORT sThumbLY;
    SHORT sThumbRX;
    SHORT sThumbRY;
} SC_XINPUT_GAMEPAD;

typedef struct {
    DWORD dwPacketNumber;
    SC_XINPUT_GAMEPAD Gamepad;
} SC_XINPUT_STATE;

typedef DWORD (WINAPI *XInputGetState_t)(DWORD, SC_XINPUT_STATE *);

static XInputGetState_t pXInputGetState = NULL;
static int xinputVersion = -1;

static void loadXInput(void) {
    HMODULE h;
    if (xinputVersion >= 0) {
        return;
    }
    xinputVersion = 0;
    h = LoadLibraryA("xinput1_4.dll");
    if (h) {
        xinputVersion = 14;
    } else {
        h = LoadLibraryA("xinput1_3.dll");
        if (h) {
            xinputVersion = 13;
        } else {
            h = LoadLibraryA("xinput9_1_0.dll");
            if (h) {
                xinputVersion = 910;
            }
        }
    }
    if (h) {
        pXInputGetState = (XInputGetState_t) GetProcAddress(h, "XInputGetState");
        if (!pXInputGetState) {
            xinputVersion = 0;
        }
    }
}

/* int xinputVersion() : 0 = no disponible */
JNIEXPORT jint JNICALL Java_com_daytona_input_joystick_NativeInput_xinputVersion(JNIEnv *env, jclass cls) {
    loadXInput();
    return (jint) xinputVersion;
}

/* int xinputState(int index, int[] out7) : 0 = OK */
JNIEXPORT jint JNICALL Java_com_daytona_input_joystick_NativeInput_xinputState(JNIEnv *env, jclass cls,
                                                                              jint index, jintArray out) {
    SC_XINPUT_STATE st;
    jint v[7];
    DWORD r;
    loadXInput();
    if (!pXInputGetState) {
        return -1;
    }
    ZeroMemory(&st, sizeof(st));
    r = pXInputGetState((DWORD) index, &st);
    if (r != 0) {
        return (jint) r;
    }
    v[0] = st.Gamepad.wButtons;
    v[1] = st.Gamepad.bLeftTrigger;
    v[2] = st.Gamepad.bRightTrigger;
    v[3] = st.Gamepad.sThumbLX;
    v[4] = st.Gamepad.sThumbLY;
    v[5] = st.Gamepad.sThumbRX;
    v[6] = st.Gamepad.sThumbRY;
    (*env)->SetIntArrayRegion(env, out, 0, 7, v);
    return 0;
}

/* int joyCaps(int id, int[] caps16, char[] name32) : 0 = OK
 * caps: vid, pid, numButtons, numAxes, xmin, xmax, ymin, ymax, zmin, zmax, rmin, rmax, umin, umax, vmin, vmax */
JNIEXPORT jint JNICALL Java_com_daytona_input_joystick_NativeInput_joyCaps(JNIEnv *env, jclass cls,
                                                                          jint id, jintArray capsOut, jcharArray nameOut) {
    JOYCAPSW c;
    jint v[16];
    jchar name[32];
    int i;
    MMRESULT r;
    ZeroMemory(&c, sizeof(c));
    r = joyGetDevCapsW((UINT_PTR) id, &c, sizeof(c));
    if (r != JOYERR_NOERROR) {
        return (jint) r;
    }
    v[0] = c.wMid;
    v[1] = c.wPid;
    v[2] = (jint) c.wNumButtons;
    v[3] = (jint) c.wNumAxes;
    v[4] = (jint) c.wXmin;  v[5] = (jint) c.wXmax;
    v[6] = (jint) c.wYmin;  v[7] = (jint) c.wYmax;
    v[8] = (jint) c.wZmin;  v[9] = (jint) c.wZmax;
    v[10] = (jint) c.wRmin; v[11] = (jint) c.wRmax;
    v[12] = (jint) c.wUmin; v[13] = (jint) c.wUmax;
    v[14] = (jint) c.wVmin; v[15] = (jint) c.wVmax;
    for (i = 0; i < 32; i++) {
        name[i] = (jchar) c.szPname[i];
    }
    (*env)->SetIntArrayRegion(env, capsOut, 0, 16, v);
    (*env)->SetCharArrayRegion(env, nameOut, 0, 32, name);
    return 0;
}

/* int joyPos(int id, int[] out8) : 0 = OK
 * out: x, y, z, r, u, v, buttons(bits), pov(centésimas de grado; 65535 = centrado) */
JNIEXPORT jint JNICALL Java_com_daytona_input_joystick_NativeInput_joyPos(JNIEnv *env, jclass cls,
                                                                         jint id, jintArray out) {
    JOYINFOEX ji;
    jint v[8];
    MMRESULT r;
    ZeroMemory(&ji, sizeof(ji));
    ji.dwSize = sizeof(ji);
    ji.dwFlags = JOY_RETURNALL;
    r = joyGetPosEx((UINT) id, &ji);
    if (r != JOYERR_NOERROR) {
        return (jint) r;
    }
    v[0] = (jint) ji.dwXpos;
    v[1] = (jint) ji.dwYpos;
    v[2] = (jint) ji.dwZpos;
    v[3] = (jint) ji.dwRpos;
    v[4] = (jint) ji.dwUpos;
    v[5] = (jint) ji.dwVpos;
    v[6] = (jint) ji.dwButtons;
    v[7] = (jint) (ji.dwPOV & 0xFFFF);
    (*env)->SetIntArrayRegion(env, out, 0, 8, v);
    return 0;
}
