import sys
from random import seed, randint

def main():
    n = int(sys.argv[2])
    ba = bytearray(n)

    seed(42)
    for i in range(0, n):
       ba[i] = randint(0, 255)

    with open(sys.argv[1], 'wb') as f:
        f.write(bytes(ba))

if __name__ == "__main__":
    main()
