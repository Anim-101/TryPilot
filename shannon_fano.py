'''
Python implementation of the Shannon-Fano algorithm for data compression.

This module provides functions to encode and decode data using the Shannon-Fano
algorithm. The Shannon-Fano algorithm is a technique for constructing
prefix codes based on a set of symbols and their probabilities (or frequencies).
It works by recursively dividing the set of symbols into two subsets with
probabilities as close as possible, and assigning '0' to one subset and '1' to
the other.
'''
from collections import Counter # Import Counter to easily count symbol frequencies

def shannon_fano_encode(data):
    """
    Encodes the input data using the Shannon-Fano algorithm.

    The process involves:
    1. Calculating the frequency of each symbol in the input data.
    2. Sorting the symbols based on their frequencies in descending order.
    3. Recursively dividing the sorted symbols into two partitions such that
       the total frequencies of each partition are as close as possible.
    4. Assigning '0' to the codes in the first partition and '1' to the codes
       in the second partition, and repeating the process for each partition.

    Args:
        data: A string or list of symbols (e.g., characters) to be encoded.

    Returns:
        A dictionary where keys are the original symbols and values are their
        corresponding Shannon-Fano binary codes (as strings).
    """
    if not data: # Handle empty input
        return {}

    # Step 1: Calculate frequency of each symbol
    # Counter will create a dictionary-like object: e.g., {'A': 5, 'B': 3, ...}
    frequency = Counter(data)

    # Step 2: Sort symbols by frequency in descending order
    # This returns a list of tuples, e.g., [('A', 5), ('B', 3), ('R', 2), ...]
    sorted_symbols = sorted(frequency.items(), key=lambda item: item[1], reverse=True)

    codes = {} # This dictionary will store the final codes for each symbol

    # Step 3 & 4: Recursively assign codes
    def assign_codes(symbols_with_freq, current_code=""):
        """
        A recursive helper function to assign binary codes to symbols.

        Args:
            symbols_with_freq: A list of (symbol, frequency) tuples, sorted by frequency.
            current_code: The binary code string built so far for the current path
                          in the Shannon-Fano tree.
        """
        if not symbols_with_freq: # Base case: no symbols left
            return

        # Base case: If there's only one symbol left in the current list,
        # assign the accumulated current_code to it. This is a leaf node in the tree.
        if len(symbols_with_freq) == 1:
            symbol, _ = symbols_with_freq[0]
            codes[symbol] = current_code if current_code else "0" # Handle single symbol case
            return

        # Find the split point: Divide the list into two sub-lists (partitions)
        # such that the sum of frequencies in each sub-list is as close as possible.
        total_freq = sum(freq for _, freq in symbols_with_freq) # Sum of all frequencies in current list
        cumulative_freq = 0 # To track the sum of frequencies for the first partition
        split_index = 0     # The index where the list will be split
        min_diff = float('inf') # Initialize with a very large difference

        # Iterate through the symbols to find the best split point
        for i, (_, freq) in enumerate(symbols_with_freq[:-1]): # Exclude last element for split
            cumulative_freq += freq
            # Difference between the sum of frequencies of the two potential partitions
            # Partition 1: symbols_with_freq[0...i]
            # Partition 2: symbols_with_freq[i+1...end]
            # Sum of Partition 1 = cumulative_freq
            # Sum of Partition 2 = total_freq - cumulative_freq
            diff = abs(cumulative_freq - (total_freq - cumulative_freq))

            if diff < min_diff:
                min_diff = diff
                split_index = i + 1 # The split occurs *after* index i
            # If the difference starts increasing, we might have passed the optimal split point,
            # especially if the cumulative frequency has already exceeded half of the total.
            # This heuristic helps in cases where multiple splits give the same minimum difference.
            elif diff >= min_diff and cumulative_freq > total_freq / 2:
                break
        
        # The split_index is determined by the loop above. 
        # If symbols_with_freq has 2+ items, the loop runs at least once, 
        # setting split_index to at least 1.
        # If symbols_with_freq has 0 or 1 item, earlier base cases handle it.

        # This case should ideally not be hit if the loop runs correctly up to symbols_with_freq[:-1]
        # elif split_index == len(symbols_with_freq) and len(symbols_with_freq) > 1:
        #     split_index = len(symbols_with_freq) - 1


        # Create the two partitions
        left_partition = symbols_with_freq[:split_index]  # First part of the list
        right_partition = symbols_with_freq[split_index:] # Second part of the list

        # Recursively call assign_codes for the left partition, appending '0' to the current code.
        assign_codes(left_partition, current_code + "0")
        # Recursively call assign_codes for the right partition, appending '1' to the current code.
        assign_codes(right_partition, current_code + "1")

    # Start the recursive process with all sorted symbols and an empty initial code.
    assign_codes(sorted_symbols)
    return codes

def shannon_fano_decode(encoded_data, codes):
    """
    Decodes the input bit string using the provided Shannon-Fano codes.

    It works by reading the encoded bit string sequentially, matching the
    accumulated bits against the known codes. Once a match is found, the
    corresponding symbol is recorded, and the process repeats for the
    remainder of the bit string.

    Args:
        encoded_data: A string of bits (e.g., "0110101") representing the
                      data encoded with Shannon-Fano.
        codes: A dictionary mapping original symbols to their Shannon-Fano
               binary codes (the output of shannon_fano_encode).

    Returns:
        The original decoded string of symbols.
    """
    if not codes: # If there are no codes (e.g., from empty input to encode), return empty.
        return ""

    # For decoding, it's more efficient to look up by code.
    # So, invert the codes dictionary: { "00": 'A', "01": 'B', ... }
    reversed_codes = {v: k for k, v in codes.items()}
    
    decoded_symbols = [] # List to store the decoded symbols
    current_code_segment = "" # Accumulates bits from encoded_data

    # Iterate through each bit in the encoded data string
    for bit in encoded_data:
        current_code_segment += bit # Add the current bit to our segment
        # Check if the accumulated segment matches any known code
        if current_code_segment in reversed_codes:
            # If it matches, we found a symbol
            symbol = reversed_codes[current_code_segment]
            decoded_symbols.append(symbol)
            current_code_segment = "" # Reset for the next symbol's code

    # Join the list of decoded symbols to form the original string
    return "".join(decoded_symbols)

# Example Usage:
# This block demonstrates how to use the encoding and decoding functions.
# It will only run when the script is executed directly (not imported as a module).
if __name__ == "__main__":
    data_to_encode = "ABRAACADABRA" # Sample data
    print(f"Original data: {data_to_encode}")

    # Encode the data. It's good practice to pass a list of characters
    # if your data is a string, to handle each character as a distinct symbol.
    sf_codes = shannon_fano_encode(list(data_to_encode))
    print(f"Shannon-Fano Codes: {sf_codes}") # Display the generated codes

    # Create the full encoded bit string by looking up the code for each symbol
    # in the original data.
    encoded_bit_string = "".join(sf_codes[symbol] for symbol in data_to_encode)
    print(f"Encoded bit string: {encoded_bit_string}")

    # Decode the bit string back to the original data
    decoded_data = shannon_fano_decode(encoded_bit_string, sf_codes)
    print(f"Decoded data: {decoded_data}")
    # Verify that the decoded data matches the original
    assert data_to_encode == decoded_data, "Mismatch between original and decoded data!"

    # Another example with a different string
    data2 = "shannon fano algorithm example"
    print(f"\nOriginal data: {data2}")
    sf_codes2 = shannon_fano_encode(list(data2)) # Encode
    print(f"Shannon-Fano Codes: {sf_codes2}")
    encoded_bit_string2 = "".join(sf_codes2[symbol] for symbol in data2) # Create bit string
    print(f"Encoded bit string: {encoded_bit_string2}")
    decoded_data2 = shannon_fano_decode(encoded_bit_string2, sf_codes2) # Decode
    print(f"Decoded data: {decoded_data2}")
    assert data2 == decoded_data2, "Mismatch in second example!"
    print("\nSuccessfully encoded and decoded both examples.")
